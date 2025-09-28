import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import ModelPlayground from "@/components/ModelPlayground";
import PromptCard from "@/components/PromptCard";
import useGenerationStore from "@/stores/generationStore";
import { TaskStatus } from "@/types/generation";
import ModelIcon from "@/components/ModelIcon";
import { Progress } from "@/components/ui/progress";
import HistoryCard from "@/components/HistoryCard";

function HomePage() {
  const { status, progress, error, pbrModelUrl, renderImageUrl } =
    useGenerationStore();

  return (
    <>
      <section className="basis-[30%]">
        <section className="flex flex-col gap-y-5">
          <Card className="flex-1">
            <CardHeader>
              <CardTitle>新建模型</CardTitle>
            </CardHeader>
            <CardContent>
              <PromptCard />
            </CardContent>
          </Card>
          {localStorage.getItem("user-auth-storage") &&
            JSON.parse(localStorage.getItem("user-auth-storage")!).state &&
            JSON.parse(localStorage.getItem("user-auth-storage")!).state.user &&
            JSON.parse(localStorage.getItem("user-auth-storage")!).state.user
              .id && <HistoryCard />}
        </section>
      </section>
      <section className="basis-[70%] flex">
        <Card className="flex-1 flex flex-col">
          <CardContent className="flex-1">
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
              <ModelPlayground
                glbUrl={pbrModelUrl || ""}
                // glbUrl="/test_models/cat/cat.glb"
              />
            )}
          </CardContent>
        </Card>
      </section>
    </>
  );
}

export default HomePage;
