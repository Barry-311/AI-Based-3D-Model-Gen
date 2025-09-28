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

export type StreamRequest = {
  prompt: string;
  texture: boolean;
  textureQuality: "standard" | "detailed";
  geometryQuality: "standard" | "detailed";
  modelSeed?: number;
  textureSeed?: number;
  faceLimit?: number;
  autoSize?: boolean;
  compression?: "meshopt" | "geometry";
};

export type StreamImageRequest = {
  file: File;
  texture: boolean;
  textureQuality: "standard" | "detailed";
  geometryQuality: "standard" | "detailed";
  style: string;
  modelSeed?: number;
  textureSeed?: number;
  faceLimit?: number;
  autoSize?: boolean;
  compression?: "meshopt" | "geometry";
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
