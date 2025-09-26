import { create } from "zustand";
import { streamTextToModel, streamImageToModel } from "@/api/testApi";
import {
  TaskStatus,
  type ResultData,
  type StreamCallbacks,
} from "@/types/generation";

type GenerationState = {
  abortController: AbortController | null;
  status: TaskStatus;
  progress: number;
  error: string | null;
  pbrModelUrl: string | null;
  renderImageUrl: string | null;
};

type GenerationActions = {
  // startGeneration: (prompt: string) => Promise<void>;
  startTextGeneration: (prompt: string) => Promise<void>;
  startImageGeneration: (file: File) => Promise<void>;
  reset: () => void;
  cleanup: () => void;
};

async function handleGeneration(
  set: (state: Partial<GenerationState>) => void,
  get: () => GenerationState & GenerationActions,
  generationFn: (
    signal: AbortSignal,
    callbacks: StreamCallbacks
  ) => Promise<void>
) {
  if (get().status === TaskStatus.RUNNING) {
    console.warn("[Zustand] A generation task is already in progress.");
    return;
  }

  get().reset(); // 开始前重置状态
  const controller = new AbortController();
  set({
    abortController: controller,
    status: TaskStatus.RUNNING,
    progress: 0,
  });

  const callbacks = {
    onProgress: (progress: number) => set({ progress }),
    onComplete: (data: ResultData) => {
      set({
        status: TaskStatus.COMPLETED,
        pbrModelUrl: data.pbrModelUrl,
        renderImageUrl: data.renderImageUrl,
        progress: 100,
      });
      get().cleanup();
    },
    onAbort: () => console.log("[Zustand] Store detected abort."),
    onError: (error: Error) => {
      set({
        status: TaskStatus.FAILED,
        error: error.message || "[Zustand] An unknown error occurred",
      });
      get().cleanup();
    },
  };

  try {
    await generationFn(controller.signal, callbacks);
  } catch (err: unknown) {
    const error =
      err instanceof Error
        ? err
        : new Error("[Zustand] An unexpected error occurred");
    set({ status: TaskStatus.FAILED, error: error.message });
    get().cleanup();
  }
}

const useGenerationStore = create<GenerationState & GenerationActions>()(
  (set, get) => ({
    abortController: null,
    status: TaskStatus.IDLE,
    progress: 0,
    error: null,
    pbrModelUrl: null,
    renderImageUrl: null,

    startTextGeneration: async (prompt: string) => {
      await handleGeneration(set, get, (signal, callbacks) =>
        streamTextToModel(prompt, signal, callbacks)
      );
    },

    startImageGeneration: async (file: File) => {
      await handleGeneration(set, get, (signal, callbacks) =>
        streamImageToModel(file, signal, callbacks)
      );
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
