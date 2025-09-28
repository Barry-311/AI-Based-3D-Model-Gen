import { Textarea } from "@/components/ui/textarea";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { z } from "zod";
import BaseForm from "./BaseForm";
import useGenerationStore from "@/stores/generationStore";
import { Switch } from "./ui/switch";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "./ui/select";
import { Tooltip, TooltipContent, TooltipTrigger } from "./ui/tooltip";
import { toast } from "sonner";
import useUserStore from "@/stores/userStore";
import { Input } from "./ui/input";

function TextForm() {
  const formSchema = z.object({
    prompt: z
      .string({
        error: "提示词不能为空",
      })
      .min(1, {
        message: "提示词不能为空",
      })
      .max(1000, {
        message: "提示词不能超过 1000 字符",
      }),
    isPublic: z.number().default(0),
    withTexture: z.boolean().default(true),
    augment: z.boolean().default(true),
    geometryQuality: z.enum(["standard", "detailed"]).default("standard"),
    textureQuality: z.enum(["standard", "detailed"]).default("standard"),
    modelSeed: z
      .coerce.number({
        error: "请输入数字",
      })
      .min(0)
      .max(10000000)
      .default(-1),
    textureSeed: z
      .coerce.number({
        error: "请输入数字",
      })
      .min(0)
      .max(10000000)
      .default(-1),
  });

  const startTextGeneration = useGenerationStore(
    (state) => state.startTextGeneration
  );
  const reset = useGenerationStore((state) => state.reset);
  const isAuthenticated = useUserStore((state) => state.isAuthenticated);

  async function handleSubmit(values: z.infer<typeof formSchema>) {
    if (isAuthenticated) {
      await startTextGeneration({
        prompt: values.prompt,
        texture: values.withTexture,
        textureQuality: values.textureQuality,
        geometryQuality: values.geometryQuality,
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
            name="prompt"
            render={({ field }) => (
              <FormItem>
                <FormLabel>提示词</FormLabel>
                <FormControl>
                  <Textarea
                    placeholder="描述你想要生成的对象"
                    maxLength={1000}
                    className="flex-1"
                    {...field}
                  />
                </FormControl>
                <FormMessage />
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
                      checked={field.value === 1}
                      onCheckedChange={(checked) =>
                        field.onChange(checked ? 1 : 0)
                      }
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
            <FormField
              control={form.control}
              name="augment"
              render={({ field }) => (
                <FormItem className="flex justify-between items-center">
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <FormLabel>自动增强提示词</FormLabel>
                    </TooltipTrigger>
                    <TooltipContent>
                      <p>自动调用大语言模型增强你的提示词</p>
                    </TooltipContent>
                  </Tooltip>
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
                  <FormLabel>模型种子</FormLabel>
                  <FormControl>
                    <Input
                      type="number"
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
                  <FormLabel>纹理种子</FormLabel>
                  <FormControl>
                    <Input
                      type="number"
                      value={field.value}
                      onChange={field.onChange}
                      className="w-[180px]"
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          </section>
        </>
      )}
    </BaseForm>
  );
}

export default TextForm;
