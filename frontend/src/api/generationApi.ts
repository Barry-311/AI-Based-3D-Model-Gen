import type {
  ResultData,
  StreamCallbacks,
  StreamImageRequest,
  StreamRequest,
} from "@/types/generation";
import { apiConfig } from "./config";

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
 */
async function streamRequest(
  url: string,
  options: RequestInit,
  callbacks: StreamCallbacks
) {
  const { onProgress, onComplete, onAbort, onError } = callbacks;
  try {
    const response = await fetch(url, { credentials: "include", ...options });

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
 * 文本生成模型
 */
function streamTextToModel(
  { prompt, texture, textureQuality, geometryQuality }: StreamRequest,
  augmented: boolean,
  signal: AbortSignal,
  callbacks: StreamCallbacks
) {
  console.log(`[SSE] 正在为提示词发送请求: "${prompt}"`);

  return streamRequest(
    // "/generate-stream",
    // "/generate-stream-actual",
    augmented ? apiConfig.generateStreamAugmented : apiConfig.generateStream,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "text/event-stream",
      },
      body: JSON.stringify({
        prompt,
        texture,
        texture_quality: textureQuality,
        geometry_quality: geometryQuality,
      }),
      signal,
    },
    callbacks
  );
}

/**
 * 图片生成模型
 */
function streamImageToModel(
  { file, texture, textureQuality, geometryQuality, style }: StreamImageRequest,
  signal: AbortSignal,
  callbacks: StreamCallbacks
) {
  console.log(`[SSE] 正在为图片发送请求: "${file.name}"`);

  const formData = new FormData();
  formData.append("file", file);
  formData.append("texture", texture ? "true" : "false");
  formData.append("texture_quality", textureQuality);
  formData.append("geometry_quality", geometryQuality);
  formData.append("style", style === "default" ? "" : style);

  return streamRequest(
    // "/generate-stream-image",
    // "/generate-stream-image-actual",
    apiConfig.generateStreamImage,
    {
      method: "POST",
      body: formData,
      signal,
    },
    callbacks
  );
}

export { streamTextToModel, streamImageToModel };
