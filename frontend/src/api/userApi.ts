import type { CurrentUser, LoginRequest, RegisterRequest } from "@/types/user";
import { apiConfig } from "./config";
import { fetchApi } from "./utils";

async function register(data: RegisterRequest) {
  return fetchApi<number>(apiConfig.userRegister, {
    method: "POST",
    body: JSON.stringify(data),
  });
}

async function login(data: LoginRequest) {
  return fetchApi<CurrentUser>(apiConfig.userLogin, {
    method: "POST",
    body: JSON.stringify(data),
  });
}

async function logout() {
  return fetchApi<boolean>(apiConfig.userLogout, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
  });
}

export { register, login, logout };
