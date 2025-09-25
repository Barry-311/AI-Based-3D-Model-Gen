export enum TaskStatus {
  IDLE = "idle",
  RUNNING = "running",
  COMPLETED = "completed",
  FAILED = "failed",
}

export type ProgressData = {
  progress: number;
}

export type ResultData = {
  pbrModelUrl: string;
  renderImageUrl: string;
}
