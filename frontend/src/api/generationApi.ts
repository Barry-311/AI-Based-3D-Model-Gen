import type { ResultData, StreamCallbacks } from "@/types/generation";
import { apiConfig } from "./config";

// const apiKey = import.meta.env.VITE_TRIPO_KEY;
// const url = import.meta.env.VITE_TRIPO_URL;

/**
 * SSE 消息解析器
 * 从数据流中提取 "event:" 和 "data:" 字段
 * @param chunk - 从数据流中读取的文本块
 */
function parseSSE(chunk: string): Array<{ type: string; data: any }> {
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
 * 发送POST请求以生成模型并以流式方式处理响应
 * @param prompt - 模型生成的提示词
 * @param signal - 用于取消请求的 AbortSignal
 * @param onProgress - 进度更新时的回调
 * @param onComplete - 完成时的回调
 * @param onAbort - 取消请求时的回调
 * @param onError - 发生错误时的回调
 */
async function streamRequest(
  url: string,
  options: RequestInit,
  callbacks: StreamCallbacks
) {
  const { onProgress, onComplete, onAbort, onError } = callbacks;
  try {
    const response = await fetch(url, options);

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
          const data = event.data as ResultData;
          if (data.status === "running") {
            console.log("[SSE] 进度更新:", event.data);
            onProgress(data.progress);
          } else if (data.status === "success") {
            console.log(`[SSE] 进度完成:`, event.data);
            onProgress(data.progress);
            onComplete(data);
          } else if (
            data.status === "banned" ||
            data.status === "cancelled" ||
            data.status === "expired" ||
            data.status === "failed" ||
            data.status === "unknown"
          ) {
            console.error("[SSE] 收到错误事件:", data.status);
            onError(new Error(data.status));
            return;
          }
        }
      }
    }
  } catch (err: any) {
    if (err.name === "AbortError") {
      console.log("[SSE] 请求已被用户中止。");
      onAbort();
    } else {
      console.error("[SSE] 处理数据流时发生错误:", err);
      onError(err as Error);
    }
  }
}

/**
 * **用于文本生成模型**
 * @param prompt - 提示词
 * @param signal - AbortSignal
 * @param callbacks - 回调函数
 */
function streamTextToModel(
  prompt: string,
  signal: AbortSignal,
  callbacks: StreamCallbacks
) {
  console.log(`[SSE] 正在为提示词发送请求: "${prompt}"`);

  return streamRequest(
    // "/generate-stream",
    // "/generate-stream-actual",
    apiConfig.generateStream,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "text/event-stream",
      },
      body: JSON.stringify({ prompt }),
      signal,
    },
    callbacks
  );
}

/**
 * **用于图片生成模型**
 * @param imageFile - 图片文件
 * @param signal - AbortSignal
 * @param callbacks - 回调函数
 */
function streamImageToModel(
  imageFile: File,
  signal: AbortSignal,
  callbacks: StreamCallbacks
) {
  console.log(`[SSE] 正在为图片发送请求: "${imageFile.name}"`);

  const formData = new FormData();
  formData.append("file", imageFile);

  return streamRequest(
    // "/generate-stream-image",
    // "/generate-stream-image-actual",
    apiConfig.generateStreamImage,
    {
      method: "POST",
      // 注意：使用 FormData 时，不要手动设置 Content-Type，浏览器会自动处理
      body: formData,
      signal,
    },
    callbacks
  );
}

export { streamTextToModel, streamImageToModel };
