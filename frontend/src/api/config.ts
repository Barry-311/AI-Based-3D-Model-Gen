const config = {
  // API基础URL配置
  baseURL: "http://localhost:8123",
  
  // API端点配置
  generateStream: "/api/app/generate-stream",
  generateStreamImage: "/api/app/generate-stream-image",
  userRegister: "/api/user/register",
  userLogin: "/api/user/login",
  userLogout: "/api/user/logout",
  getModelsByPage: "/api/model/list/page/vo",
  getModelById: "/api/model/get",
};

export { config as apiConfig };
