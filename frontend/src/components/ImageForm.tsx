import { useEffect, useState } from "react";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { z } from "zod";
import type { ControllerRenderProps } from "react-hook-form";
import BaseForm from "./BaseForm";

// 允许的的图片MIME类型
const ACCEPTED_IMAGE_TYPES = [
  "image/jpeg",
  "image/jpg",
  "image/png",
  "image/webp",
];
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 最大文件大小 (10MB)

function ImageForm() {
  const [previewImageUrl, setPreviewImageUrl] = useState<string | null>(null);

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
        "只支持 .jpg, .jpeg, .png 和 .webp 格式的图片"
      ),
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
    await new Promise((resolve) => setTimeout(resolve, 2000));
    console.log(values);
  }

  return (
    <BaseForm
      onSubmit={handleSubmit}
      schema={formSchema}
      submitButtonText={{ default: "生成", submitting: "正在生成..." }}
    >
      {(form) => (
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
      )}
    </BaseForm>
  );
}

export default ImageForm;
