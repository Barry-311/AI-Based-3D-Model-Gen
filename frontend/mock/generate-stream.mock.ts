import { defineMock, createSSEStream } from "vite-plugin-mock-dev-server";

// 模拟3D模型生成任务
export default defineMock({
  url: "/generate-stream",
  method: "POST",
  response: (req, res) => {
    // 从请求体中获取 prompt
    const { prompt } = req.body;
    const sse = createSSEStream(req, res);

    // 初始化任务状态
    let progress = 0;
    const createTime = new Date().toISOString();
    const taskData = {
      id: Date.now(),
      taskId: `mock_${Math.random().toString(36).substring(2, 12)}`,
      prompt: prompt || "a cute cat sitting on a chair",
      status: "queued",
      progress: 0,
      pbrModelUrl: null,
      renderedImageUrl: null,
      fileSize: null,
      createTime: createTime,
      updateTime: createTime,
    } as any;

    // 立即发送一个 "queued" 状态
    sse.write({
      event: "progress",
      data: JSON.stringify({ ...taskData, status: "queued" }),
    });

    let interval;

    // 延迟 1 秒后开始模拟 "running" 状态
    setTimeout(() => {
      taskData.status = "running";
      // 创建一个定时器，每 200毫秒 更新一次进度
      interval = setInterval(() => {
        progress += Math.floor(Math.random() * 5) + 1; // 每次增加 1-5 的进度
        if (progress >= 100) {
          progress = 100;
          taskData.status = "success";
          taskData.pbrModelUrl = "https://placehold.co/model.glb";
          taskData.renderedImageUrl = `https://placehold.co/600x400/cccccc/ffffff?text=Model+for+${encodeURIComponent(
            taskData.prompt
          )}`;
          taskData.fileSize = Math.floor(Math.random() * 2000000) + 1000000;
        }

        // 更新任务数据
        taskData.progress = progress;
        taskData.updateTime = new Date().toISOString();

        // 将更新后的数据作为 "progress" 事件发送给前端
        sse.write({
          event: "progress",
          data: JSON.stringify(taskData),
        });

        // 如果任务完成，清除定时器并关闭 SSE 连接
        if (progress >= 100) {
          clearInterval(interval);
          sse.end();
        }
      }, 30);
    }, 1000);

    // 监听客户端断开连接的事件，以清理资源
    req.on("close", () => {
      console.log("Client disconnected. Clearing interval.");
      clearInterval(interval);
    });
  },
});
