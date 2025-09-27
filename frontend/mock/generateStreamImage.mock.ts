import { defineMock, createSSEStream } from "vite-plugin-mock-dev-server";

// 模拟图片转3D模型任务 (纯前端模拟)
export default defineMock({
  url: "/generate-stream-image",
  method: "POST",
  response: (req, res) => {
    const body: any = req.body;
    const fileField = body?.file;
    const file = Array.isArray(fileField) ? fileField[0] : fileField;

    const sse = createSSEStream(req, res);

    if (!file) {
      console.error("Mock Error: No file uploaded.");
      sse.write({
        event: "error",
        data: JSON.stringify({ message: "错误：未上传文件" }),
      });
      sse.end();
      return;
    }

    const fileName = Array.isArray(file)
      ? file[0].originalFilename
      : file.originalFilename;

    // 初始化任务状态
    let progress = 0;
    const createTime = new Date().toISOString();
    const taskData = {
      id: Date.now(),
      taskId: `mock_img_${Math.random().toString(36).substring(2, 12)}`,
      status: "queued",
      progress: 0,
      originalImageUrl:
        "https://placehold.co/400x300/a7a7a7/ffffff?text=Uploaded+Image",
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

    let interval: NodeJS.Timeout;

    // 延迟 1 秒后开始模拟 "running" 状态
    setTimeout(() => {
      taskData.status = "running";
      interval = setInterval(() => {
        progress += Math.floor(Math.random() * 5) + 1; // 每次增加 1-5 的进度
        if (progress >= 100) {
          progress = 100;
          taskData.status = "success";
          taskData.pbrModelUrl = "https://placehold.co/model.glb";
          taskData.renderedImageUrl = `https://placehold.co/600x400/cccccc/ffffff?text=Model+for+${encodeURIComponent(
            fileName
          )}`;
          taskData.fileSize = Math.floor(Math.random() * 2000000) + 1000000;
        }

        taskData.progress = progress;
        taskData.updateTime = new Date().toISOString();

        sse.write({
          event: "progress",
          data: JSON.stringify(taskData),
        });

        if (progress >= 100) {
          clearInterval(interval);
          sse.end();
        }
      }, 20); // 调整更新频率以模拟真实感
    }, 1000);

    // 监听客户端断开连接的事件
    req.on("close", () => {
      console.log("Client disconnected from image-mock. Clearing interval.");
      clearInterval(interval);
    });
  },
});
