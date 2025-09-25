import { defineMock, createSSEStream } from "vite-plugin-mock-dev-server";

// const TRIPO_API_KEY = process.env.VITE_TRIPO_KEY;
// const TRIPO_API_URL = process.env.VITE_TRIPO_URL as string;
const TRIPO_API_KEY = "__PLACEHOLDER__";
const TRIPO_API_URL = "https://api.tripo3d.ai/v2/openapi/task";

const mapToModel3DVO = (apiResponse, prompt) => {
  return {
    id: Date.now(), // Mock a database ID
    taskId: apiResponse.task_id,
    prompt: prompt,
    status: apiResponse.status,
    progress: apiResponse.progress,
    pbrModelUrl: apiResponse.output?.pbr_model || null,
    renderedImageUrl: apiResponse.output?.rendered_image || null,
    fileSize: apiResponse.output?.pbr_model_size || null,
    createTime: apiResponse.create_time,
    updateTime: new Date().toISOString(),
  };
};

export default defineMock({
  url: "/generate-stream-actual",
  method: "POST",
  response: async (req, res) => {
    const { prompt } = req.body;
    const sse = createSSEStream(req, res);

    console.log("TRIPO_API_KEY: ", TRIPO_API_KEY)
    console.log("TRIPO_API_URL: ", TRIPO_API_URL)

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

    let pollingInterval;

    // 客户端断开连接时，停止轮询
    req.on("close", () => {
      console.log("客户端已断开连接，停止轮询。");
      clearInterval(pollingInterval);
    });

    try {
      // 步骤 1: 调用 Tripo3D API 创建任务
      console.log(`[Tripo3D] 正在为提示词创建任务: "${prompt}"`);
      const createTaskResponse = await fetch(TRIPO_API_URL, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${TRIPO_API_KEY}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          type: "text_to_model",
          prompt: prompt,
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
          const frontendData = mapToModel3DVO(taskStatus, prompt);

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
            console.log(`[Tripo3D] 模型 URL: ${taskStatus.output["pbr_model"]}`);
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
      console.error("调用 Tripo3D API 时发生错误:", error);
      sse.write({
        event: "error",
        data: JSON.stringify({ message: error.message }),
      });
      sse.end();
    }
  },
});
