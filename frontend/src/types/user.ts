export type RegisterRequest = {
  userAccount: string;
  userPassword: string;
  checkPassword: string;
};

export type LoginRequest = {
  userAccount: string;
  userPassword: string;
};

export type CurrentUser = {
  id: number;
  userAccount: string;
  userName: string;
  userAvatar: string;
  userProfile: string;
  userRole: string;
  createTime: string;
  updateTime: string;
};

export type FeedbackRequest = {
  rating: number;
  title: string;
  content: string;
};

export interface FeedbackPagedRequest {
  pageNum?: number;
  pageSize?: number;
  sortField?: string;
  sortOrder?: "asc" | "desc" | "ascend" | "descend" | "";
  id?: number;
  userId?: number;
  rating?: number;
  title?: string;
  content?: string;
  createTime?: string;
}

export interface FeedbackPagedResponse {
  records: {
    id: number;
    userId: number;
    feedbackType: string;
    rating: number;
    title: string;
    content: string;
    createTime: string;
    updateTime: string;
  }[];
  pageNumber: number;
  pageSize: number;
  totalPage: number;
  totalRow: number;
  optimizeCountQuery: boolean;
}
