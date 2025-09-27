// const baseUrl = `https://stubbly-onomatologic-jaxton.ngrok-free.dev/api`;
const baseUrl = `http://118.25.143.47/api`;

const config = {
  // mock
  // generateStream: "/generate-stream",
  // generateStreamImage: "/generate-stream-image",
  // userRegister: "/api/user/register",
  // userLogin: "/api/user/login",
  // userLogout: "/api/user/logout",
  // getModelsByPage: "/api/model/list/page/vo",
  // getModelById: "/api/model/get",

  generateStream: `${baseUrl}/app/generate-stream`,
  generateStreamAugmented: `${baseUrl}/app/generate-stream-augmented`,
  generateStreamImage: `${baseUrl}/app/generate-stream-image`,
  userRegister: `${baseUrl}/user/register`,
  userLogin: `${baseUrl}/user/login`,
  userLogout: `${baseUrl}/user/logout`,
  getModelsByPage: `${baseUrl}/model/list/page/vo`,
  getModelById: `${baseUrl}/model/get`,
};

export { config as apiConfig };
