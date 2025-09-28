import z from "zod";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { cn } from "@/lib/utils";
import { Input } from "./ui/input";
import { Textarea } from "./ui/textarea";
import BaseForm from "./BaseForm";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "./ui/form";
import { feedback } from "@/api/userApi";

const feedbackSchema = z.object({
  rating: z.coerce
    .number()
    .min(1, { message: "评分最低为 1" })
    .max(5, { message: "评分最高为 5" }),
  title: z.string().max(100, { message: "标题不能超过100个字符" }),
  content: z
    .string()
    .min(1, { message: "反馈内容至少需要 1 个字符" })
    .max(1000, { message: "反馈内容不能超过1000个字符" }),
});

interface IFeedbackFormProps extends React.ComponentProps<"div"> {
  onSuccess?: () => void;
}

export function FeedbackForm({
  className,
  onSuccess,
  ...props
}: IFeedbackFormProps) {
  const form = useForm<z.infer<typeof feedbackSchema>>();

  async function handleSubmit(data: z.infer<typeof feedbackSchema>) {
    try {
      await feedback({
        rating: data.rating,
        title: data.title,
        content: data.content,
      });

      toast.success("反馈提交成功");
      form.reset({ rating: 0, title: "", content: "" });
      if (onSuccess) {
        onSuccess();
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "发生未知错误";
      form.setError("root.serverError", {
        type: "manual",
        message: errorMessage,
      });
      toast.error(errorMessage);
    }
  }

  return (
    <div className={cn("flex flex-col gap-6", className)} {...props}>
      <BaseForm
        schema={feedbackSchema}
        onSubmit={handleSubmit}
        defaultValues={{ rating: 0, title: "", content: "" }}
        submitButtonText={{
          default: "提交反馈",
          submitting: "提交中...",
        }}
        className="space-y-4"
        submitButtonClassName="w-full mt-4"
      >
        {(form) => (
          <>
            <FormField
              control={form.control}
              name="rating"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>请给我们的模型生成质量打分 (1-5)</FormLabel>
                  <FormControl>
                    <Input
                      type="number"
                      min={1}
                      max={5}
                      placeholder="请给我们的模型生成质量打分"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="title"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>标题</FormLabel>
                  <FormControl>
                    <Input placeholder="请输入反馈标题" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="content"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>具体内容</FormLabel>
                  <FormControl>
                    <Textarea
                      placeholder="请详细描述您遇到的问题或建议..."
                      rows={5}
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          </>
        )}
      </BaseForm>
    </div>
  );
}
