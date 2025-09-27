type BaseResponse<T> = {
  code: number;
  data: T;
  message: string;
};

/**
 * 封装的 fetch 函数，用于处理通用逻辑
 * @param endpoint API 路径
 * @param options fetch 的配置
 */
async function fetchApi<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<BaseResponse<T>> {
  // 导入配置
  const { apiConfig } = await import('./config');
  
  // 构建完整的URL
  const url = endpoint.startsWith('http') ? endpoint : `${apiConfig.baseURL}${endpoint}`;
  
  const response = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
  });

  if (!response.ok) {
    // 处理 HTTP 错误 (例如 500, 404)
    throw new Error(`网络错误: ${response.statusText}`);
  }

  const result: BaseResponse<T> = await response.json();

  // 处理业务错误 (例如 code !== 0)
  if (result.code !== 0) {
    throw new Error(result.message || "发生未知错误");
  }

  return result;
}

export { fetchApi };
