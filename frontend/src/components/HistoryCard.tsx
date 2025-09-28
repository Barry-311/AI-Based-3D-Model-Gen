import { useCallback, useEffect, useRef, useState } from "react";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useModelStore } from "@/stores/useModelStore";
import type { Model } from "@/types/model";
import { Button } from "./ui/button";
import ModelDetailCard from "./ModelDetailCard";
import { IconCaretDownFilled, IconCaretRightFilled } from "@tabler/icons-react";

interface IEntryProps {
  model: Model;
  onDelete: (id: number) => void;
  onUpdate: (id: number, isPublic: boolean) => void;
}

function Entry({ model, onDelete, onUpdate }: IEntryProps) {
  const handleDelete = () => {
    onDelete(model.id);
  };
  const handleUpdate = () => {
    onUpdate(model.id, !model.isPublic);
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
          <span className="text-sm text-muted-foreground overflow-ellipsis">
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
            {model.prompt ? (
              <ModelDetailCard
                glbUrl={model.pbrModelUrl}
                creater="你"
                createTime={new Date(Number(model.createTime)).toLocaleString()}
                prompt={model.prompt}
              />
            ) : (
              model.pictureUrl && (
                <ModelDetailCard
                  glbUrl={model.pbrModelUrl}
                  creater="你"
                  createTime={model.createTime}
                  image={model.pictureUrl}
                />
              )
            )}
          </DialogContent>
        </Dialog>
        <Button variant="link" size="sm" onClick={handleUpdate}>
          {model.isPublic ? "设为私有" : "设为公开"}
        </Button>
        <Button variant="destructive" size="sm" onClick={handleDelete}>
          删除
        </Button>
      </div>
    </div>
  );
}

function HistoryCard() {
  const [isHistoryOpen, setIsHistoryOpen] = useState(true);

  const { models, fetchModels, hasMore, isLoading, deleteModel, updateModel } =
    useModelStore();

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
    if (
      models.length === 0 &&
      hasMore &&
      localStorage.getItem("user-auth-storage")
    ) {
      const userId = JSON.parse(localStorage.getItem("user-auth-storage")!)
        .state.user.id;
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
