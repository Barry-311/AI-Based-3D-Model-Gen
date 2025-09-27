import type { HTMLAttributes } from "react";
import ModelPlayground from "./ModelPlayground";
import { cn } from "@/lib/utils";

interface IModelDetailCardBaseProps extends HTMLAttributes<HTMLDivElement> {
  glbUrl: string;
  creater: string;
  createTime: string;
}

type IModelDetailCardProps =
  | (IModelDetailCardBaseProps & { prompt: string; image?: never })
  | (IModelDetailCardBaseProps & { image: string; prompt?: never });

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
      {prompt ? (
        <div className="font-medium text-wrap">
          提示词:
          {prompt}
        </div>
      ) : (
        <div>{<img src={image} />}</div>
      )}
      <div className="text-muted-foreground">
        由 {creater} 创建于 {createTime}
      </div>
      {/* <div className="text-muted-foreground">点赞 收藏</div> */}
    </div>
  );
}

export default ModelDetailCard;
