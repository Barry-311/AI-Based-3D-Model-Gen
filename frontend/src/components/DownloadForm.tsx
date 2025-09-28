// 在 DownloadForm.tsx 中
import { Button } from "./ui/button"; // 确保引入 Button
import { toast } from "sonner";
import { useState } from "react";

// 从 GLBModel.tsx 导入类型定义
// import type { ExporterFunction } from './GLBModel'; 

// 或者在这里重新定义
type ExporterFunction = (filename: string) => Promise<void>;

interface IDownloadFormProps {
  exporters: {
    exportWithTextures: ExporterFunction | null;
    exportWithoutTextures: ExporterFunction | null;
  };
}

// 这个组件现在变得简单多了，不再需要 form
function DownloadForm({ exporters }: IDownloadFormProps) {
  const [isDownloading, setIsDownloading] = useState(false);

  const handleDownload = async (exportFunc: ExporterFunction | null, filename: string) => {
    if (!exportFunc) {
      toast.error("导出功能尚未准备好，请稍候。");
      return;
    }
    setIsDownloading(true);
    try {
      await exportFunc(filename);
    } finally {
      setIsDownloading(false);
    }
  };

  const isReady = exporters.exportWithTextures && exporters.exportWithoutTextures;

  return (
    <div className="p-1 space-y-3">
      <div className="space-y-1">
        <h4 className="font-medium leading-none">下载模型</h4>
        <p className="text-sm text-muted-foreground">
          选择要导出的模型版本。
        </p>
      </div>
      <div className="flex flex-col space-y-2">
        <Button
          onClick={() => handleDownload(exporters.exportWithTextures, 'model_with_textures.glb')}
          disabled={!isReady || isDownloading}
        >
          {isDownloading ? "处理中..." : "下载带纹理的模型 (.glb)"}
        </Button>
        <Button
          variant="secondary"
          onClick={() => handleDownload(exporters.exportWithoutTextures, 'model_no_textures.glb')}
          disabled={!isReady || isDownloading}
        >
          {isDownloading ? "处理中..." : "下载无纹理的模型 (.glb)"}
        </Button>
      </div>
       {!isReady && <p className="text-xs text-center text-gray-500">正在加载导出器...</p>}
    </div>
  );
}

export default DownloadForm;