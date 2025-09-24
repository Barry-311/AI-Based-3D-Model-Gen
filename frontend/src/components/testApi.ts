const apiKey = import.meta.env.VITE_TRIPO_KEY;
const url = import.meta.env.VITE_TRIPO_URL;

/**
 * SSE 消息解析器
 * 从数据流中提取 "event:" 和 "data:" 字段
 * @param {string} chunk - 从数据流中读取的文本块
 * @returns {Array<{type: string, data: any}>}
 */
function parseSSE(chunk: string): Array<{ type: string; data: any; }> {
  const events = [];
  const lines = chunk.split("\n\n");
  for (const line of lines) {
    if (!line) continue;
    let eventType = "message";
    let eventData = "";
    const fieldLines = line.split("\n");
    for (const fieldLine of fieldLines) {
      const separatorIndex = fieldLine.indexOf(":");
      if (separatorIndex === -1) continue;
      const field = fieldLine.slice(0, separatorIndex);
      const value = fieldLine.slice(separatorIndex + 1).trim();
      if (field === "event") {
        eventType = value;
      } else if (field === "data") {
        eventData = value;
      }
    }
    if (eventData) {
      try {
        events.push({ type: eventType, data: JSON.parse(eventData) });
      } catch (e) {
        console.error("无法解析 SSE 的 JSON 数据:", eventData);
      }
    }
  }
  return events;
}

/**
 * 发送POST请求以生成模型并以流式方式处理响应。
 * @param {string} prompt - 模型生成的提示词。
 * @param {AbortSignal} signal - 用于取消请求的 AbortSignal。
 */
async function streamModelGeneration(prompt: string, signal: AbortSignal) {
  try {
    console.log(`[SSE] 正在为提示词发送请求: "${prompt}"`);

    const response = await fetch("/generate-stream-actual", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "text/event-stream",
      },
      body: JSON.stringify({ prompt }),
      signal,
    });

    if (!response.ok) {
      throw new Error(`[SSE] HTTP 错误，状态: ${response.status}`);
    }

    if (!response.body) {
      throw new Error("[SSE] 响应体为空");
    }

    const reader = response.body
      .pipeThrough(new TextDecoderStream())
      .getReader();

    while (true) {
      const { done, value } = await reader.read();
      if (done) {
        console.log("[SSE] 数据流结束");
        break;
      }

      const events = parseSSE(value);
      for (const event of events) {
        if (event.type === "progress") {
          console.log("[SSE] 进度更新:", event.data);
        } else {
          console.log(`[SSE] 收到事件 '${event.type}':`, event.data);
        }
      }
    }
  } catch (err: any) {
    if (err.name === "AbortError") {
      console.log("[SSE] 请求已被用户中止。");
    } else {
      console.error("[SSE] 处理数据流时发生错误:", err);
    }
  }
}

export { streamModelGeneration };
