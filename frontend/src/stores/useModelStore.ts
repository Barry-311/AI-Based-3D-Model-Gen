import { create } from "zustand";
import { getModelsByPage } from "@/api/modelApi";
import type { Model } from "@/types/model";

const PAGE_SIZE = 18;

interface ModelStoreState {
  models: Model[];
  page: number;
  hasMore: boolean;
  isLoading: boolean;
  error: Error | null;
}

interface ModelStoreActions {
  fetchModels: () => Promise<void>;
  reset: () => void;
}

export const useModelStore = create<ModelStoreState & ModelStoreActions>(
  (set, get) => ({
    models: [],
    page: 1,
    hasMore: true,
    isLoading: false,
    error: null,

    fetchModels: async () => {
      if (get().isLoading || !get().hasMore) return;

      set({ isLoading: true, error: null });

      try {
        const currentPage = get().page;
        // 调用 API，pageNum 可能需要根据后端是从0还是1开始来调整 (currentPage - 1 或 currentPage)
        const response = await getModelsByPage({
          pageNum: currentPage,
          pageSize: PAGE_SIZE,
          sortField: "createTime",
          sortOrder: "descend",
        });

        const newModels = response.data.records || [];

        set((state) => ({
          models:
            currentPage === 1 ? newModels : [...state.models, ...newModels],
          page: state.page + 1,
          hasMore:
            newModels.length === PAGE_SIZE && state.page < response.data.totalPage,
          isLoading: false,
        }));
      } catch (error) {
        set({ isLoading: false, error: error as Error });
        throw error;
      }
    },

    reset: () => {
      set({
        models: [],
        page: 1,
        hasMore: true,
        isLoading: false,
        error: null,
      });
    },
  })
);
