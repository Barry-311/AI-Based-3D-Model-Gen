import { useState } from "react";
import z from "zod";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "./ui/form";
import BaseForm from "./BaseForm";
import { Checkbox } from "./ui/checkbox";
import { toast } from "sonner";

const options = [
  {
    id: "downloadObj",
    label: "下载 obj 文件",
  },
  {
    id: "downloadMtl",
    label: "下载 mtl 文件",
  },
] as const;

function DownloadForm() {
  const formSchema = z.object({
    options: z.array(z.string()).refine((value) => value.some((item) => item), {
      message: "至少选择一项开始下载",
    }),
  });

  async function handleDownload(fileUrl: string, downloadName: string) {
    const response = await fetch(fileUrl);

    if (!response.ok) {
      throw new Error(`下载时发生错误: ${response.statusText}`);
    }

    const blob = await response.blob();
    const url = URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", downloadName);
    document.body.appendChild(link);
    link.click();

    if (link.parentNode) {
      link.parentNode.removeChild(link);
    }
    URL.revokeObjectURL(url);
  }

  async function handleSubmit(values: z.infer<typeof formSchema>) {
    await new Promise((resolve) => setTimeout(resolve, 2000));
    console.log(values);
    try {
      toast("开始下载");
      const downloadPromises: Promise<void>[] = [];

      if (values.options.includes("downloadObj")) {
        downloadPromises.push(
          handleDownload("/windmill/windmill.obj", "model.obj")
        );
      }
      if (values.options.includes("downloadMtl")) {
        downloadPromises.push(
          handleDownload("/windmill/windmill.mtl", "model.mtl")
        );
      }

      if (downloadPromises.length > 0) {
        await Promise.all(downloadPromises);
      }
    } catch (error) {
      if (error instanceof Error) {
        toast.error(`下载时发生错误: ${error}`);
      } else {
        toast.error("发生未知错误");
      }
    }
  }

  return (
    <BaseForm
      schema={formSchema}
      onSubmit={handleSubmit}
      defaultValues={{ options: options.map((item) => item.id) }}
      submitButtonText={{
        default: "下载",
        submitting: "正在下载...",
      }}
    >
      {(form) => (
        <FormField
          control={form.control}
          name="options"
          render={({ field }) => (
            <FormItem className="mb-2">
              <FormLabel className="text-base">下载选项</FormLabel>
              {options.map((option) => (
                <FormItem
                  key={option.id}
                  className="flex flex-row items-center gap-2"
                >
                  <FormControl>
                    <Checkbox
                      checked={field.value?.includes(option.id)}
                      onCheckedChange={(checked) => {
                        return checked
                          ? field.onChange([...field.value, option.id])
                          : field.onChange(
                              field.value?.filter(
                                (value: (typeof options)[number]["id"]) =>
                                  value !== option.id
                              )
                            );
                      }}
                    />
                  </FormControl>
                  <FormLabel className="text-sm font-normal">
                    {option.label}
                  </FormLabel>
                </FormItem>
              ))}
              <FormMessage />
            </FormItem>
          )}
        />
      )}
    </BaseForm>
  );
}

export default DownloadForm;
