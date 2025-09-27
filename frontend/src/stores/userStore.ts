import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import type { CurrentUser, LoginRequest } from "@/types/user";
import { login, logout } from "@/api/userApi";

type UserState = {
  user: CurrentUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
};

const useUserStore = create<UserState>()(
  // 使用 persist 中间件进行持久化
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,
      isLoading: false,

      /**
       * 登录 Action
       */
      login: async (data) => {
        set({ isLoading: true });
        try {
          const response = await login(data);
          // 登录成功，更新 user 状态
          set({ user: response.data, isAuthenticated: true });
        } catch (error) {
          console.error("登录失败:", error);
          throw error;
        } finally {
          // 无论成功或失败，最后都结束 loading
          set({ isLoading: false });
        }
      },

      /**
       * 登出 Action
       */
      logout: async () => {
        set({ isLoading: true });
        try {
          await logout();
          // 登出成功，清空 user 状态
          set({ user: null, isAuthenticated: false });
        } catch (error) {
          console.error("退出失败:", error);
          throw error;
        } finally {
          set({ isLoading: false });
        }
      },
    }),
    {
      name: "user-auth-storage", // localStorage 中的 key
      storage: createJSONStorage(() => localStorage), // 默认使用 localStorage
    }
  )
);

export default useUserStore;
