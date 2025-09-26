// mock/user.mock.ts
import { defineMock } from "vite-plugin-mock-dev-server";

const mockUsers = {
  admin: {
    id: 1,
    userAccount: "admin",
    userName: "管理员",
    userAvatar: "https://i.pravatar.cc/150?u=admin",
    userProfile: "系统管理员，拥有最高权限。",
    userRole: "admin",
    // 模拟 ISO 8601 格式的日期时间
    createTime: new Date("2023-01-15T10:30:00Z").toISOString(),
    updateTime: new Date().toISOString(),
    password: "password123", // 仅用于mock校验
  },
  user: {
    id: 2,
    userAccount: "user",
    // userName: "普通用户",
    userAvatar: "https://i.pravatar.cc/150?u=user",
    userProfile: "一个热爱技术分享的普通用户。",
    userRole: "user",
    createTime: new Date("2023-05-20T18:00:00Z").toISOString(),
    updateTime: new Date().toISOString(),
    password: "password123",
  },
};

export default defineMock([
  // =================================================================
  // 1. 用户注册接口
  // =================================================================
  {
    url: "/api/user/register",
    method: "POST",
    body: (request) => {
      const { userAccount, userPassword, checkPassword } = request.body;

      if (!userAccount || !userPassword || !checkPassword) {
        return { code: 40000, data: null, message: "请求参数不能为空" };
      }
      if (mockUsers[userAccount]) {
        // 检查账户是否已存在于模拟数据库
        return {
          code: 40002,
          data: null,
          message: `账户 '${userAccount}' 已存在`,
        };
      }
      if (userPassword !== checkPassword) {
        return { code: 40001, data: null, message: "两次输入的密码不一致" };
      }

      const newUserId = new Date().getTime();
      return { code: 0, data: newUserId, message: "注册成功" };
    },
  },

  // =================================================================
  // 2. 用户登录接口
  // =================================================================
  {
    url: "/api/user/login",
    method: "POST",
    body: (request) => {
      const { userAccount, userPassword } = request.body;

      // 场景1: 检查参数
      if (!userAccount || !userPassword) {
        return {
          code: 40000,
          data: null,
          message: "账号或密码不能为空",
        };
      }

      // 场景2: 查找用户
      const user = mockUsers[userAccount];
      if (!user) {
        return {
          code: 40100,
          data: null,
          message: "该账号未注册",
        };
      }

      // 场景3: 校验密码
      if (user.password !== userPassword) {
        return {
          code: 40101,
          data: null,
          message: "密码错误",
        };
      }

      // 场景4: 登录成功
      // 返回的用户信息不应包含密码
      const { password, ...loginUserVO } = user;

      // 每次登录成功时都更新一下 updateTime
      loginUserVO.updateTime = new Date().toISOString();

      return {
        code: 0,
        data: loginUserVO,
        message: "登录成功",
      };
    },
  },

  // =================================================================
  // 3. 用户退出接口
  // =================================================================
  {
    url: "/api/user/logout",
    method: "POST",
    // 退出登录通常不需要复杂的逻辑，直接返回成功即可
    body: {
      code: 0,
      data: true,
      message: "退出成功",
    },
  },
]);
