import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { cn } from "@/lib/utils";
import type { HTMLAttributes } from "react";
import TextForm from "./TextForm";
import ImageForm from "./ImageForm";

interface IPromptCard extends HTMLAttributes<HTMLDivElement> {}

function PromptCard({ className }: IPromptCard) {
  return (
    <Tabs
      defaultValue="text2model"
      className={cn("flex flex-col gap-y-5 h-full w-full", className)}
    >
      <TabsList>
        <TabsTrigger value="text2model">文本转 3D 模型</TabsTrigger>
        <TabsTrigger value="image2model">图片转 3D 模型</TabsTrigger>
      </TabsList>
      <TabsContent value="text2model">
        <TextForm />
      </TabsContent>
      <TabsContent value="image2model">
        <ImageForm />
      </TabsContent>
    </Tabs>
  );
}

export default PromptCard;
