// mock/model.mock.ts
import { defineMock } from "vite-plugin-mock-dev-server";
import type { Model, ModelPagedRequest } from "../src/types/model";

const TOTAL_MODELS = 500;
const allModels: Model[] = [];

for (let i = 0; i < TOTAL_MODELS; i++) {
  const id = TOTAL_MODELS - i; // ID 倒序，让最新的数据在最前面
  allModels.push({
    id: id,
    taskId: `task_${Date.now()}_${id}`,
    name: `AI 生成的精致小狗模型 ${id}`,
    // 让 prompt 内容多样化
    prompt: `一只可爱的${
      ["柯基", "哈士奇", "金毛", "萨摩耶"][i % 4]
    }，粘土风格，辛烷渲染，傑作 ${id}`,
    status: "succeed",
    progress: 100,
    // 固定的 glb 模型用于测试
    pbrModelUrl: "/test_models/dog/dog.glb",
    // 使用 picsum.photos 生成随机图片，通过 random 参数避免浏览器缓存
    renderedImageUrl: `https://picsum.photos/500/500?random=${id}`,
    pictureUrl: `https://picsum.photos/500/500?random=${id}`,
    // 模拟 10 个不同的用户
    userId: 1000 + (i % 10),
    // 从今天开始，每天往前推一天，模拟真实创建时间
    createTime: Date.now().toString(),
  });
}

// =================================================================
// 定义 Mock 接口
// =================================================================
export default defineMock([
  {
    url: "/api/model/list/page/vo",
    method: "POST",
    // 使用 response 函数动态处理分页逻辑
    response: (req, res) => {
      // 从请求体中获取分页参数
      const { pageNum = 1, pageSize = 10 } = req.body as ModelPagedRequest;

      console.log(
        `[Mock] 接收到请求: pageNum=${pageNum}, pageSize=${pageSize}`
      );

      // 计算分页逻辑
      const totalRow = allModels.length;
      const totalPage = Math.ceil(totalRow / pageSize);
      const startIndex = (pageNum - 1) * pageSize;
      const endIndex = startIndex + pageSize;

      // 使用 slice 模拟数据库分页查询
      const records = allModels.slice(startIndex, endIndex);

      // 模拟 1000ms 的网络延迟
      setTimeout(() => {
        res.end(
          JSON.stringify({
            code: 0,
            message: "请求成功",
            data: {
              records: records,
              pageNumber: pageNum,
              pageSize: pageSize,
              totalPage: totalPage,
              totalRow: totalRow,
              optimizeCountQuery: true,
            },
          })
        );
      }, 1000);
    },
  },
]);
