import { useEffect, useState } from "react";
import type { ControllerRenderProps } from "react-hook-form";
import { z } from "zod";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "./ui/form";
import { Switch } from "./ui/switch";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "./ui/select";
import { Input } from "./ui/input";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "./ui/collapsible";
import { Button } from "./ui/button";
import { ChevronsUpDown } from "lucide-react";
import BaseForm from "./BaseForm";
import useGenerationStore from "@/stores/generationStore";
import { toast } from "sonner";
import useUserStore from "@/stores/userStore";
import { Tooltip, TooltipContent, TooltipTrigger } from "./ui/tooltip";

const ACCEPTED_IMAGE_TYPES = ["image/jpeg", "image/jpg", "image/png"]; // 允许的的图片MIME类型
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 最大文件大小 (10MB)

function ImageForm() {
  const [previewImageUrl, setPreviewImageUrl] = useState<string | null>(null);
  const [isAdvancedSettingOpen, setIsAdvancedSettingOpen] = useState(false);

  const startImageGeneration = useGenerationStore(
    (state) => state.startImageGeneration
  );

  const isAuthenticated = useUserStore((state) => state.isAuthenticated);

  useEffect(() => {
    // 当 previewUrl 改变或组件卸载时清理旧的 URL
    return () => {
      if (previewImageUrl) {
        URL.revokeObjectURL(previewImageUrl);
      }
    };
  }, [previewImageUrl]);

  const formSchema = z.object({
    image: z
      .instanceof(File, { message: "请选择一张图片" })
      .refine((file) => file.size <= MAX_FILE_SIZE, `图片大小不能超过 10MB`)
      .refine(
        (file) => ACCEPTED_IMAGE_TYPES.includes(file.type),
        "只支持 .jpg, .jpeg, .png 格式的图片"
      ),
    isPublic: z.boolean().default(true),
    withTexture: z.boolean().default(true),
    geometryQuality: z.enum(["standard", "detailed"]).default("standard"),
    textureQuality: z.enum(["standard", "detailed"]).default("standard"),
    style: z.string().default("default"),
    modelSeed: z.coerce
      .number({
        error: "请输入数字",
      })
      .min(0)
      .max(10000000)
      .default(-1),
    textureSeed: z.coerce
      .number({
        error: "请输入数字",
      })
      .min(0)
      .max(10000000)
      .default(-1),
  });

  function handleChange(
    e: React.ChangeEvent<HTMLInputElement>,
    field: ControllerRenderProps<any, "image">
  ) {
    const file = e.target.files?.[0];
    field.onChange(file);

    if (previewImageUrl) {
      URL.revokeObjectURL(previewImageUrl);
    }

    if (
      file &&
      file.size <= MAX_FILE_SIZE &&
      ACCEPTED_IMAGE_TYPES.includes(file.type)
    ) {
      setPreviewImageUrl(URL.createObjectURL(file));
    } else {
      setPreviewImageUrl(null);
    }
  }

  async function handleSubmit(values: z.infer<typeof formSchema>) {
    if (isAuthenticated) {
      await startImageGeneration({
        file: values.image,
        style: values.style,
        texture: values.withTexture,
        geometryQuality: values.geometryQuality,
        textureQuality: values.textureQuality,
        modelSeed: values.modelSeed,
        textureSeed: values.textureSeed,
      });
    } else {
      toast.error("请先登录");
    }
  }

  return (
    <BaseForm
      onSubmit={handleSubmit}
      schema={formSchema}
      submitButtonText={{ default: "生成", submitting: "正在生成..." }}
    >
      {(form) => (
        <>
          <FormField
            control={form.control}
            name="image"
            render={({ field }) => (
              <FormItem>
                <FormLabel>参考图</FormLabel>
                <FormControl>
                  <Input
                    id="image"
                    type="file"
                    accept="image/*"
                    className="flex-1"
                    onBlur={field.onBlur}
                    name={field.name}
                    ref={field.ref}
                    onChange={(e) => handleChange(e, field)}
                  />
                </FormControl>
                <FormMessage />
                {previewImageUrl && (
                  <img
                    src={previewImageUrl}
                    alt="图片预览"
                    className="max-w-full max-h-60 h-auto rounded-md border"
                  />
                )}
              </FormItem>
            )}
          />
          <section className="flex flex-col gap-y-4">
            <FormField
              control={form.control}
              name="isPublic"
              render={({ field }) => (
                <FormItem className="hidden justify-between items-center">
                  <FormLabel>公开发布到模型库</FormLabel>
                  <FormControl>
                    <Switch
                      defaultChecked
                      checked={field.value}
                      onCheckedChange={field.onChange}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="withTexture"
              render={({ field }) => (
                <FormItem className="flex justify-between items-center">
                  <FormLabel>生成纹理</FormLabel>
                  <FormControl>
                    <Switch
                      defaultChecked
                      checked={field.value}
                      onCheckedChange={field.onChange}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Collapsible
              open={isAdvancedSettingOpen}
              onOpenChange={setIsAdvancedSettingOpen}
            >
              <CollapsibleTrigger asChild>
                <Button
                  variant="ghost"
                  size="icon"
                  className="w-full justify-between"
                >
                  <span>高级设置</span>
                  <ChevronsUpDown />
                </Button>
              </CollapsibleTrigger>
              <CollapsibleContent className="flex flex-col gap-2">
                <FormField
                  control={form.control}
                  name="style"
                  render={({ field }) => (
                    <FormItem className="flex justify-between items-center">
                      <FormLabel>模型风格</FormLabel>
                      <FormControl>
                        <Select
                          defaultValue="default"
                          value={field.value}
                          onValueChange={field.onChange}
                        >
                          <SelectTrigger className="w-[180px]">
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectGroup>
                              <SelectItem value="default">默认</SelectItem>
                              <SelectItem value="person:person2cartoon">
                                人物：卡通
                              </SelectItem>
                              <SelectItem value="object:clay">
                                物体：粘土
                              </SelectItem>
                              <SelectItem value="object:steampunk">
                                物体：蒸汽朋克
                              </SelectItem>
                              <SelectItem value="animal:venom">
                                动物：毒液
                              </SelectItem>
                              <SelectItem value="object:barbie">
                                物体：芭比娃娃
                              </SelectItem>
                              <SelectItem value="object:christmas">
                                物体：圣诞
                              </SelectItem>
                              <SelectItem value="gold">黄金</SelectItem>
                              <SelectItem value="ancient_bronze">
                                古代青铜
                              </SelectItem>
                            </SelectGroup>
                          </SelectContent>
                        </Select>
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="geometryQuality"
                  render={({ field }) => (
                    <FormItem className="flex justify-between items-center">
                      <FormLabel>模型质量</FormLabel>
                      <FormControl>
                        <Select
                          defaultValue="standard"
                          value={field.value}
                          onValueChange={field.onChange}
                        >
                          <SelectTrigger className="w-[180px]">
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectGroup>
                              <SelectItem value="standard">标准</SelectItem>
                              <SelectItem value="detailed">精细</SelectItem>
                            </SelectGroup>
                          </SelectContent>
                        </Select>
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="textureQuality"
                  render={({ field }) => (
                    <FormItem className="flex justify-between items-center">
                      <FormLabel>纹理质量</FormLabel>
                      <FormControl>
                        <Select
                          defaultValue="standard"
                          value={field.value}
                          onValueChange={field.onChange}
                        >
                          <SelectTrigger className="w-[180px]">
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectGroup>
                              <SelectItem value="standard">标准</SelectItem>
                              <SelectItem value="detailed">精细</SelectItem>
                            </SelectGroup>
                          </SelectContent>
                        </Select>
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="modelSeed"
                  render={({ field }) => (
                    <FormItem className="flex justify-between items-center">
                      <Tooltip>
                        <TooltipTrigger asChild>
                          <FormLabel>模型种子</FormLabel>
                        </TooltipTrigger>
                        <TooltipContent>
                          <p>相同的种子可以生成相同的结果，默认使用随机种子</p>
                        </TooltipContent>
                      </Tooltip>
                      <FormControl>
                        <Input
                          type="number"
                          min={0}
                          value={field.value}
                          onChange={field.onChange}
                          className="w-[180px]"
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="textureSeed"
                  render={({ field }) => (
                    <FormItem className="flex justify-between items-center">
                      <Tooltip>
                        <TooltipTrigger asChild>
                          <FormLabel>纹理种子</FormLabel>
                        </TooltipTrigger>
                        <TooltipContent>
                          <p>相同的种子可以生成相同的结果，默认使用随机种子</p>
                        </TooltipContent>
                      </Tooltip>
                      <FormControl>
                        <Input
                          type="number"
                          min={0}
                          value={field.value}
                          onChange={field.onChange}
                          className="w-[180px]"
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="faceLimit"
                  render={({ field }) => (
                    <FormItem className="flex justify-between items-center">
                      <FormLabel>面数上限</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          min={1000}
                          max={16000}
                          value={field.value}
                          onChange={field.onChange}
                          className="w-[180px]"
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="autoSize"
                  render={({ field }) => (
                    <FormItem className="flex justify-between items-center">
                      <Tooltip>
                        <TooltipTrigger asChild>
                          <FormLabel>自动调整大小</FormLabel>
                        </TooltipTrigger>
                        <TooltipContent>
                          <p>根据真实世界大小自动调整模型尺寸</p>
                        </TooltipContent>
                      </Tooltip>
                      <FormControl>
                        <Switch
                          checked={field.value}
                          onCheckedChange={field.onChange}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="compression"
                  render={({ field }) => (
                    <FormItem className="flex justify-between items-center">
                      <FormLabel>模型压缩</FormLabel>
                      <FormControl>
                        <Select
                          defaultValue="meshopt"
                          value={field.value}
                          onValueChange={field.onChange}
                        >
                          <SelectTrigger className="w-[180px]">
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectGroup>
                              <SelectItem value="meshopt">
                                Meshopt 压缩
                              </SelectItem>
                              <SelectItem value="geometry">
                                Geometry 压缩
                              </SelectItem>
                            </SelectGroup>
                          </SelectContent>
                        </Select>
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </CollapsibleContent>
            </Collapsible>
          </section>
        </>
      )}
    </BaseForm>
  );
}

export default ImageForm;
