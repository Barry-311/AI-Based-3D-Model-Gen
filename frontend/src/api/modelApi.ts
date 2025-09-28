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
  const urlWithParams = `${apiConfig.getModelById}?id=${data.id}`;

  return fetchApi<Model>(urlWithParams, {
    method: "GET",
  });
}

async function deleteModelById(data: ModelRequest) {
  return fetchApi<ModelPagedResponse>(apiConfig.deleteModelById, {
    method: "POST",
    body: JSON.stringify(data),
  });
}

async function updateModelById(data: ModelRequest) {
  return fetchApi<ModelPagedResponse>(apiConfig.updateModelById, {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export { getModelsByPage, getModelById, deleteModelById, updateModelById };
