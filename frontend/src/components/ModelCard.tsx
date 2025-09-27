import { useState } from "react";
import ModelDetailCard from "./ModelDetailCard";
import { Badge } from "./ui/badge";
import { Card, CardContent, CardFooter } from "./ui/card";
import { Dialog, DialogContent, DialogTrigger } from "./ui/dialog";
import { Skeleton } from "./ui/skeleton";

interface IModelCardBaseProps {
  glbUrl: string;
  renderImage: string;
  creater: string;
  createTime: string;
}

type IModelCardProps =
  | (IModelCardBaseProps & { prompt: string; image?: never })
  | (IModelCardBaseProps & { image: string; prompt?: never });

function ModelCard({
  glbUrl,
  renderImage,
  creater,
  createTime,
  prompt,
  image,
}: IModelCardProps) {
  const [loaded, setLoaded] = useState(false);

  return (
    <Dialog>
      <DialogTrigger asChild>
        <Card className="w-full hover:cursor-pointer hover:shadow-lg transition-shadow duration-300">
          <CardContent>
            <div className="aspect-[16/9] w-full relative">
              {!loaded && <Skeleton className="w-full h-full" />}
              <img
                src={renderImage}
                className={`w-full h-full object-cover ${
                  loaded ? "block" : "hidden"
                }`}
                onLoad={() => setLoaded(true)}
                onError={() => setLoaded(true)}
              />
            </div>
          </CardContent>
          <CardFooter className="flex-col items-start gap-3 text-sm">
            <Badge variant="outline">
              {prompt ? "由文本提示词生成" : "由参考图生成"}
            </Badge>
            {prompt ? (
              <div className="line-clamp-1 font-medium">{prompt}</div>
            ) : (
              <div>查看图片 {image}</div>
            )}
            <div className="text-muted-foreground">
              由 {creater} 创建于 {createTime}
            </div>
          </CardFooter>
        </Card>
      </DialogTrigger>
      <DialogContent className="!max-w-[80vw] !min-h-[80vh]">
        {prompt ? (
          <ModelDetailCard
            glbUrl={glbUrl}
            creater={creater}
            createTime={createTime}
            prompt={prompt}
          />
        ) : (
          image && (
            <ModelDetailCard
              glbUrl={glbUrl}
              creater={creater}
              createTime={createTime}
              image={image}
            />
          )
        )}
      </DialogContent>
    </Dialog>
  );
}

export default ModelCard;
