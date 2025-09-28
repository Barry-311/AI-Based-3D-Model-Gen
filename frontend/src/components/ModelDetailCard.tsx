import type { HTMLAttributes } from "react";
import ModelPlayground from "./ModelPlayground";
import { cn } from "@/lib/utils";

interface IModelDetailCardBaseProps extends HTMLAttributes<HTMLDivElement> {
  glbUrl: string;
  creater: string;
  createTime: string;
}

type IModelDetailCardProps = IModelDetailCardBaseProps & {
  prompt: string;
  image: string;
};

function ModelDetailCard({
  glbUrl,
  creater,
  createTime,
  prompt,
  image,
  className,
}: IModelDetailCardProps) {
  return (
    <div className={cn("flex flex-col gap-2 overflow-y-scroll", className)}>
      <ModelPlayground glbUrl={glbUrl} />
      {prompt !== "图片转模型" ? (
        <div className="font-medium text-wrap">提示词: {prompt}</div>
      ) : (
        <div>
          <img src={image} className="w-[100px]" />
        </div>
      )}
      <div className="text-muted-foreground">
        由 {creater} 创建于 {createTime}
      </div>
    </div>
  );
}

export default ModelDetailCard;
