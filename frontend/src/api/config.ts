// const baseUrl = `https://stubbly-onomatologic-jaxton.ngrok-free.dev`;
const baseUrl = `http://118.25.143.47`;

const config = {
  // mock
  // generateStream: "/generate-stream",
  // generateStreamImage: "/generate-stream-image",
  // generateStreamAugmented: "/generate-stream-image",
  // userRegister: "/api/user/register",
  // userLogin: "/api/user/login",
  // userLogout: "/api/user/logout",
  // getModelsByPage: "/api/model/list/page/vo",
  // getModelById: "/api/model/get",
  // deleteModelById: "api/model/delete",
  // updateModelById: "api/model/update"

  generateStream: `${baseUrl}/api/app/generate-stream`,
  generateStreamAugmented: `${baseUrl}/api/app/generate-stream-augmented`,
  generateStreamImage: `${baseUrl}/api/app/generate-stream-image`,
  userRegister: `${baseUrl}/api/user/register`,
  userLogin: `${baseUrl}/api/user/login`,
  userLogout: `${baseUrl}/api/user/logout`,
  getModelsByPage: `${baseUrl}/api/model/list/page/vo`,
  getModelById: `${baseUrl}/api/model/get`,
  deleteModelById: `${baseUrl}/api/model/delete`,
  updateModelById: `${baseUrl}/api/model/update`,
  feedback: `${baseUrl}/api/feedback/add`,
  getFeedbackByPage: `${baseUrl}/api/feedback/list/page/vo`,
};

export { config as apiConfig };
