export enum TaskStatus {
  IDLE = "idle",
  RUNNING = "running",
  COMPLETED = "completed",
  FAILED = "failed",
}

export interface StreamCallbacks {
  onProgress: (progress: number) => void;
  onComplete: (data: ResultData) => void;
  onAbort: () => void;
  onError: (error: Error) => void;
}

export type ProgressData = {
  progress: number;
};

export type ResultData = {
  id: number;
  taskId: string;
  prompt: string;
  status:
    | "running"
    | "success"
    | "queued"
    | "failed"
    | "banned"
    | "expired"
    | "cancelled"
    | "unknown";
  progress: number;
  pbrModelUrl: string;
  renderImageUrl: string;
  fileSize: number;
  createTime: string;
  updateTime: string;
};
