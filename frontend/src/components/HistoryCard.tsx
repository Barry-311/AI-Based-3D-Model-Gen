import { useCallback, useEffect, useRef, useState } from "react";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useModelStore } from "@/stores/useModelStore";
import type { Model } from "@/types/model";
import { Button } from "./ui/button";
import ModelDetailCard from "./ModelDetailCard";
import { IconCaretDownFilled, IconCaretRightFilled } from "@tabler/icons-react";
import useUserStore from "@/stores/userStore";
import { TaskStatus } from "@/types/generation";
import useGenerationStore from "@/stores/generationStore";

interface IEntryProps {
  model: Model;
  onDelete: (id: number) => void;
  onUpdate: (id: number, isPublic: number) => void;
}

function Entry({ model, onDelete }: IEntryProps) {
  const handleDelete = () => {
    onDelete(model.id);
  };
  return (
    <div className="p-2 my-2 border-b last:border-b-0 flex justify-between items-center">
      <div>
        {model.pictureUrl ? (
          <img
            src={model.pictureUrl}
            alt="Generated image"
            className="rounded-md object-cover"
          />
        ) : (
          <span className="text-sm text-muted-foreground line-clamp-3 overflow-ellipsis">
            {model.prompt}
          </span>
        )}
      </div>
      <div className="mt-2 flex items-center justify-end">
        <Dialog>
          <DialogTrigger asChild>
            <Button variant="link" size="sm">
              查看
            </Button>
          </DialogTrigger>
          <DialogContent className="!max-w-[80vw] !min-h-[80vh]">
            <DialogTitle className="sr-only">模型详情</DialogTitle>
            <ModelDetailCard
              glbUrl={model.pbrModelUrl}
              creater="你"
              createTime={model.createTime}
              prompt={model.prompt}
              image={model.pictureUrl}
            />
          </DialogContent>
        </Dialog>
        <Dialog>
          <DialogTrigger asChild>
            <Button variant="link" size="sm">
              删除
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>你确定要删除吗？</DialogTitle>
              <DialogDescription className="sr-only">
                你确定要删除吗？
              </DialogDescription>
            </DialogHeader>
            <DialogFooter>
              <DialogClose asChild>
                <Button variant="outline">取消</Button>
              </DialogClose>
              <Button variant="destructive" onClick={handleDelete}>
                确认删除
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
    </div>
  );
}

function HistoryCard() {
  const [isHistoryOpen, setIsHistoryOpen] = useState(true);

  const {
    models,
    fetchModels,
    hasMore,
    isLoading,
    deleteModel,
    updateModel,
    reset,
  } = useModelStore();

  const isAuthenticated = useUserStore((state) => state.isAuthenticated);
  const userId = useUserStore((state) => state.user?.id);
  const generationStatus = useGenerationStore((state) => state.status);

  useEffect(() => {
    return () => {
      reset();
    };
  }, [reset]);

  const observer = useRef<IntersectionObserver | null>(null);

  const lastElementRef = useCallback(
    (node: HTMLDivElement | null) => {
      if (isLoading) return;

      if (observer.current) observer.current.disconnect();

      observer.current = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasMore) {
          fetchModels();
        }
      });

      if (node) observer.current.observe(node);
    },
    [isLoading, hasMore, fetchModels]
  );

  useEffect(() => {
    if (generationStatus === TaskStatus.COMPLETED) {
      console.log("Generation task completed. Refreshing history list...");

      reset();
      fetchModels(userId);
    }
  }, [generationStatus, fetchModels, reset, userId]);

  useEffect(() => {
    if (models.length === 0 && hasMore && isAuthenticated) {
      fetchModels(userId);
    }
  }, [models.length, hasMore, fetchModels]);

  return (
    <Collapsible
      open={isHistoryOpen}
      onOpenChange={setIsHistoryOpen}
      className="flex-1"
    >
      <Card className="flex-1">
        <CollapsibleTrigger asChild>
          <CardHeader>
            <CardTitle className="flex gap-3 items-center hover:cursor-pointer select-none">
              {isHistoryOpen ? (
                <IconCaretDownFilled />
              ) : (
                <IconCaretRightFilled />
              )}{" "}
              历史记录
            </CardTitle>
          </CardHeader>
        </CollapsibleTrigger>
        <CollapsibleContent>
          <CardContent className="max-h-[30vh] overflow-y-scroll">
            {models.map((model, index) => {
              if (models.length === index + 1) {
                return (
                  <div ref={lastElementRef} key={model.id}>
                    <Entry
                      model={model}
                      onDelete={deleteModel}
                      onUpdate={updateModel}
                    />
                  </div>
                );
              } else {
                return (
                  <Entry
                    key={model.id}
                    model={model}
                    onDelete={deleteModel}
                    onUpdate={updateModel}
                  />
                );
              }
            })}
            {isLoading && <div className="text-center p-4">加载中...</div>}
            {!hasMore && models.length > 0 && (
              <div className="text-center text-sm text-muted-foreground p-4">
                没有更多记录了
              </div>
            )}
          </CardContent>
        </CollapsibleContent>
      </Card>
    </Collapsible>
  );
}

export default HistoryCard;
