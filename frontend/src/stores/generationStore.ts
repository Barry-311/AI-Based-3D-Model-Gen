import { create } from "zustand";
import { streamModelGeneration } from "@/components/testApi";
import {
  TaskStatus,
  type ProgressData,
  type ResultData,
} from "@/types/generation";

type GenerationState = {
  abortController: AbortController | null;
  status: TaskStatus;
  progress: number;
  error: string | null;
  pbrModelUrl: string | null;
  renderImageUrl: string | null;
}

type GenerationActions = {
  startGeneration: (prompt: string) => Promise<void>;
  reset: () => void;
  cleanup: () => void;
}

const useGenerationStore = create<GenerationState & GenerationActions>()(
  (set, get) => ({
    abortController: null,
    status: TaskStatus.IDLE,
    progress: 0,
    error: null,
    pbrModelUrl: null,
    renderImageUrl: null,

    startGeneration: async (prompt: string) => {
      if (get().status === TaskStatus.RUNNING) {
        console.warn("[Zustand] Generation is already in progress.");
        return;
      }

      get().reset();
      const controller = new AbortController();
      set({
        abortController: controller,
        status: TaskStatus.RUNNING,
        progress: 0,
      });

      try {
        await streamModelGeneration({
          prompt,
          signal: controller.signal,
          onProgress: (progress: number) => {
            set({ progress: progress });
          },
          onComplete: (data: ResultData) => {
            console.log("===data===", data)
            set({
              status: TaskStatus.COMPLETED,
              pbrModelUrl: data.pbrModelUrl,
              renderImageUrl: data.renderImageUrl,
              progress: 100,
            });
            get().cleanup();
          },
          onAbort: () => {
            console.log("[Zustand] Store detected abort.");
          },
          onError: (error: Error) => {
            set({
              status: TaskStatus.FAILED,
              error: error.message || "[Zustand] An unknown error occurred",
            });
            get().cleanup();
          },
        });
      } catch (err: unknown) {
        if (err instanceof Error) {
          set({ status: TaskStatus.FAILED, error: err.message });
        } else {
          set({
            status: TaskStatus.FAILED,
            error: "[Zustand] An unexpected error occurred",
          });
        }
        get().cleanup();
      }
    },

    reset: () => {
      get().abortController?.abort();
      set({
        abortController: null,
        status: TaskStatus.IDLE,
        progress: 0,
        error: null,
        pbrModelUrl: null,
        renderImageUrl: null,
      });
    },

    cleanup: () => {
      set({ abortController: null });
    },
  })
);

export default useGenerationStore;
