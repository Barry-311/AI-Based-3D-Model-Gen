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
