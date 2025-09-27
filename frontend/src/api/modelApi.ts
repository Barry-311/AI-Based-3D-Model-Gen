import type { ModelPagedRequest } from "@/types/model";
import { apiConfig } from "./config";

async function getModelsByPage(params: ModelPagedRequest) {
  try {
    const response = await fetch(apiConfig.getModelsByPage, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      // 注意：接口文档中请求体有一个根参数 model3DQueryRequest
      // 但实际开发中，后端框架可能直接接收平铺的 JSON 对象。
      // 这里我们假设后端能直接处理。如果不行，需要包装成 { "model3DQueryRequest": params }
      body: JSON.stringify(params),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();

    if (result.code !== 0) {
      throw new Error(result.message || "Failed to fetch data");
    }

    // 直接返回成功响应中的 data 部分
    return result.data;
  } catch (error) {
    console.error("Failed to fetch models:", error);
    throw error; // 将错误向上抛出，以便 store 能捕获
  }
}

export { getModelsByPage };
