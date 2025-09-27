import { defineMock, createSSEStream } from "vite-plugin-mock-dev-server";
import { readFile } from "fs/promises";
import { TRIPO_KEY } from "./mock.key";

const TRIPO_API_KEY = TRIPO_KEY;
const TRIPO_API_URL = "https://api.tripo3d.ai/v2/openapi/task";
const TRIPO_UPLOAD_URL = "https://api.tripo3d.ai/v2/openapi/upload/sts";

// 将模拟的API响应映射为前端需要的VO (Value Object)
const mapImageTaskToModel3DVO = (apiResponse, originalFilename) => {
  return {
    id: Date.now(), // 模拟一个数据库ID
    taskId: apiResponse.task_id,
    status: apiResponse.status,
    progress: apiResponse.progress,
    originalImageUrl: apiResponse.original_image_url || null,
    pbrModelUrl: apiResponse.output?.pbr_model || null,
    renderedImageUrl: apiResponse.output?.rendered_image || null,
    fileSize: apiResponse.output?.pbr_model_size || null,
    createTime: apiResponse.create_time,
    updateTime: new Date().toISOString(),
  };
};

export default defineMock({
  url: "/generate-stream-image-actual",
  method: "POST",
  response: async (req, res) => {
    const body: any = req.body;
    const fileField = body?.file;
    const file = Array.isArray(fileField) ? fileField[0] : fileField;

    const sse = createSSEStream(req, res);

    console.log("fileField", fileField);
    console.log("file", file);

    if (!file || !file.filepath) {
      console.error("错误：未上传文件");
      sse.write({
        event: "error",
        data: JSON.stringify({ message: "错误：未上传文件" }),
      });
      sse.end();
      return;
    }

    if (!TRIPO_API_KEY) {
      console.error(
        "错误: Tripo3D API 密钥未设置。请在 .env.local 文件中设置 VITE_TRIPO_API_KEY。"
      );
      sse.write({
        event: "error",
        data: JSON.stringify({ message: "服务器端 API 密钥未配置" }),
      });
      sse.end();
      return;
    }

    let pollingInterval: NodeJS.Timeout;

    // 客户端断开连接时，停止轮询
    req.on("close", () => {
      console.log("Client disconnected, stopping polling for image task.");
      clearInterval(pollingInterval);
    });

    try {
      console.log(`[Tripo3D] 正在从路径读取文件: ${file.filepath}`);
      const fileBuffer = await readFile(file.filepath);

      console.log(`[Tripo3D] 准备上传图片: "${file.originalFilename}"`);
      const uploadFormData = new FormData();

      // 从读取到的 buffer 创建 Blob
      const fileBlob = new Blob([fileBuffer], { type: file.mimetype });
      uploadFormData.append("file", fileBlob, file.originalFilename);

      const uploadResponse = await fetch(TRIPO_UPLOAD_URL, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${TRIPO_API_KEY}`,
        },
        body: uploadFormData,
      });

      if (!uploadResponse.ok) {
        const errorBody = await uploadResponse.text();
        // 尝试解析JSON以获得更详细的错误信息
        try {
            const errorJson = JSON.parse(errorBody);
            throw new Error(`上传图片失败: ${uploadResponse.status} - ${errorJson.message || errorBody}`);
        } catch {
            throw new Error(`上传图片失败: ${uploadResponse.status} ${errorBody}`);
        }
      }

      const uploadJson = await uploadResponse.json();
      const imageToken = uploadJson.data?.image_token;

      if (!imageToken) {
        throw new Error("从上传响应中未能获取 image_token");
      }
      console.log(`[Tripo3D] 图片上传成功, image_token: ${imageToken}`);

      // 步骤 1: 模拟调用外部API创建任务
      console.log(`[Tripo3D] 正在为图片创建任务: "${imageToken}"`);
      const createTaskResponse = await fetch(TRIPO_API_URL, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${TRIPO_API_KEY}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          type: "image_to_model",
          file: {
            file_token: imageToken,
          },
        }),
      });

      if (!createTaskResponse.ok) {
        const errorBody = await createTaskResponse.text();
        throw new Error(
          `创建任务失败: ${createTaskResponse.status} ${errorBody}`
        );
      }

      const json = await createTaskResponse.json();
      const task_id = json.data["task_id"];
      console.log(`[Tripo3D] 任务创建成功，任务ID: ${task_id}`);

      // 步骤 2: 开始轮询任务状态
      pollingInterval = setInterval(async () => {
        try {
          const pollResponse = await fetch(`${TRIPO_API_URL}/${task_id}`, {
            headers: { Authorization: `Bearer ${TRIPO_API_KEY}` },
          });

          if (!pollResponse.ok) {
            // 如果轮询失败，但不是致命错误，可以继续尝试
            console.warn(
              `轮询任务 ${task_id} 失败，状态码: ${pollResponse.status}`
            );
            return;
          }

          const json = await pollResponse.json();
          const taskStatus = json.data;
          const frontendData = mapImageTaskToModel3DVO(taskStatus, file.originalFilename);

          // 步骤 3: 将轮询结果通过 SSE 推送给前端
          sse.write({
            event: "progress",
            data: JSON.stringify(frontendData),
          });

          console.log(
            `[Tripo3D] 轮询状态: ${taskStatus.status}, 进度: ${taskStatus.progress}%`
          );

          // 步骤 4: 如果任务已完成（或失败），停止轮询并关闭 SSE 连接
          const finalizedStates = [
            "success",
            "failed",
            "banned",
            "expired",
            "cancelled",
            "unknown",
          ];
          if (finalizedStates.includes(taskStatus.status)) {
            console.log(`[Tripo3D] 任务已完成，最终状态: ${taskStatus.status}`);
            console.log(
              `[Tripo3D] 模型 URL: ${taskStatus.output["pbr_model"]}`
            );
            clearInterval(pollingInterval);
            sse.end();
          }
        } catch (pollError) {
          console.error(`轮询期间发生错误:`, pollError);
          clearInterval(pollingInterval);
          sse.write({
            event: "error",
            data: JSON.stringify({ message: "轮询任务状态失败" }),
          });
          sse.end();
        }
      }, 3000); // 每 3 秒轮询一次
    } catch (error) {
      console.error("Error simulating API call:", error);
      sse.write({
        event: "error",
        data: JSON.stringify({ message: error.message }),
      });
      sse.end();
    }
  },
});
