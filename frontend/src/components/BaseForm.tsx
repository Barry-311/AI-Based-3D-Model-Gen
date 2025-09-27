import { Form } from "@/components/ui/form";
import { Button } from "@/components/ui/button";
import {
  useForm,
  type SubmitHandler,
  type UseFormReturn,
} from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z, ZodType } from "zod";
import { cn } from "@/lib/utils";

interface IBaseForm<T extends ZodType<any, any>> {
  schema: ZodType<T, any, any>;
  onSubmit: SubmitHandler<z.infer<T>>;
  children: (form: UseFormReturn<z.infer<T>>) => React.ReactNode;
  submitButtonText: {
    default: string;
    submitting: string;
  };
  defaultValues?: z.infer<T>;
  className?: string;
  submitButtonClassName?: string;
}

/**
 * 通用表单组件
 * @param schema - Zod 验证 schema
 * @param onSubmit - 表单提交时的回调函数
 * @param children - 设置表单的字段 (FormField)
 * @param submitButtonText - 提交按钮的文本
 * @param defaultValues - 表单的默认值
 * @param className - Tailwind 类名
 * @param submitButtonClassName - Tailwind 类名
 */
function BaseForm<T extends ZodType<any, any>>({
  schema,
  onSubmit,
  children,
  submitButtonText,
  defaultValues,
  className,
  submitButtonClassName,
}: IBaseForm<T>) {
  type FormValues = z.infer<T>;

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues,
  });

  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit(onSubmit)}
        className={cn("space-y-8", className)}
      >
        {children(form)}
        <Button
          type="submit"
          disabled={form.formState.isSubmitting}
          className={cn(submitButtonClassName)}
        >
          {form.formState.isSubmitting
            ? submitButtonText.submitting
            : submitButtonText.default}
        </Button>
      </form>
    </Form>
  );
}

export default BaseForm;
