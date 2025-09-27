import type {
  Model,
  ModelPagedRequest,
  ModelPagedResponse,
  ModelRequest,
} from "@/types/model";
import { apiConfig } from "./config";
import { fetchApi } from "./utils";

async function getModelsByPage(data: ModelPagedRequest) {
  return fetchApi<ModelPagedResponse>(apiConfig.getModelsByPage, {
    method: "POST",
    body: JSON.stringify(data),
  });
}

async function getModelById(data: ModelRequest) {
  // 构造带查询参数的 URL
  const urlWithParams = `${apiConfig.getModelById}?id=${data.id}`;

  // 使用 GET 方法，并且不需要 body
  return fetchApi<Model>(urlWithParams, {
    method: "GET",
  });
}

export { getModelsByPage, getModelById };
