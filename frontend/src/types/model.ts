export interface Model {
  id: number;
  taskId: string;
  name: string;
  prompt: string;
  status: string;
  progress: number;
  pbrModelUrl: string;
  renderedImageUrl: string;
  pictureUrl: string;
  userId: number;
  createTime: string; // ISO 8601 date string, e.g., "2025-01-01T12:00:00Z"
}

export interface ModelPagedRequest {
  pageNum?: number;
  pageSize?: number;
  sortField?: string;
  sortOrder?: "asc" | "desc" | "ascend" | "descend" | "";
  id?: number;
  taskId?: string;
  name?: string;
  prompt?: string;
  status?: string;
  userId?: number;
  createTime?: string;
}  

export interface ModelPagedResponse {
  records: Model[];
  pageNumber: number;
  pageSize: number;
  totalPage: number;
  totalRow: number;
  optimizeCountQuery: boolean;
}

export interface ModelRequest {
  id: number;
}
