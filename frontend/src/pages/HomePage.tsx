import { IconDownload } from "@tabler/icons-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import ModelPlayground from "@/components/ModelPlayground";
import PromptCard from "@/components/PromptCard";
import DownloadForm from "@/components/DownloadForm";
import useGenerationStore from "@/stores/generationStore";
import { TaskStatus } from "@/types/generation";
import ModelIcon from "@/components/ModelIcon";
import { Progress } from "@/components/ui/progress";

function HomePage() {
  const { status, progress, error, pbrModelUrl, renderImageUrl } =
    useGenerationStore();

  const modelPlaygroundDownloadControl = (
    <Popover>
      <PopoverTrigger>
        <Button variant="outline">
          <IconDownload />
        </Button>
      </PopoverTrigger>
      <PopoverContent>
        <DownloadForm />
      </PopoverContent>
    </Popover>
  );

  return (
    <>
      <section className="basis-[30%]">
        <Card className="flex-1">
          <CardHeader>
            <CardTitle>新建模型</CardTitle>
          </CardHeader>
          <CardContent>
            <PromptCard />
          </CardContent>
        </Card>
      </section>
      <section className="basis-[70%] flex">
        <Card className="flex-1 flex flex-col">
          <CardContent className="flex-1">
            {/* <ModelPlayground /> */}

            {status !== TaskStatus.COMPLETED ? (
              <div className="h-full w-full flex justify-center items-center">
                {status === TaskStatus.IDLE && (
                  <div className="text-gray-400 select-none">
                    使用提示词或上传图片开始生成模型
                  </div>
                )}
                {status === TaskStatus.RUNNING && (
                  <div className="w-full flex flex-col items-center gap-10">
                    <ModelIcon />
                    <Progress value={progress} className="w-[60%]" />
                    <span>正在生成...</span>
                  </div>
                )}
                {status === TaskStatus.FAILED && <div>生成时发生错误</div>}
              </div>
            ) : (
              // 任务完成后，渲染通用的 ModelViewer 组件
              <ModelPlayground
                glbUrl={pbrModelUrl || ""}
                customControls={modelPlaygroundDownloadControl}
              />
            )}
          </CardContent>
        </Card>
      </section>
    </>
  );
}

export default HomePage;
