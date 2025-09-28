import z from "zod";
import { cn } from "@/lib/utils";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import BaseForm from "./BaseForm";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "./ui/form";
import useUserStore from "@/stores/userStore";
import { toast } from "sonner";
import { useForm } from "react-hook-form";

interface ILoginFormProps {
  onSwitchToRegister: () => void;
}

const loginSchema = z.object({
  username: z.string().min(1, { message: "用户名不能为空" }),
  password: z.string().min(6, { message: "密码至少需要 6 位" }),
});

export function LoginForm({
  onSwitchToRegister,
  className,
  ...props
}: ILoginFormProps & React.ComponentProps<"div">) {
  const form = useForm<z.infer<typeof loginSchema>>();
  const { login } = useUserStore();

  async function handleSubmit(data: z.infer<typeof loginSchema>) {
    try {
      await login({
        userAccount: data.username,
        userPassword: data.password,
      });

      toast.success("登录成功！");
    } catch (error) {
      if (error instanceof Error) {
        form.setError("root.serverError", {
          type: "manual",
          message: error.message,
        });
        toast.error(error.message);
      } else {
        toast.error("发生未知错误");
      }
    }
  }

  return (
    <div className={cn("flex flex-col gap-6", className)} {...props}>
      <BaseForm
        schema={loginSchema}
        onSubmit={handleSubmit}
        defaultValues={{ username: "", password: "" }}
        submitButtonText={{
          default: "登录",
          submitting: "登录中...",
        }}
        className="space-y-4"
        submitButtonClassName="w-full mt-4"
      >
        {(form) => (
          <>
            <FormField
              control={form.control}
              name="username"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>用户名</FormLabel>
                  <FormControl>
                    <Input placeholder="请输入用户名" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="password"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>密码</FormLabel>
                  <FormControl>
                    <Input
                      type="password"
                      placeholder="请输入密码"
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
      <div className=" text-center text-sm">
        还没有账号？
        <Button variant="link" onClick={onSwitchToRegister}>
          立即注册
        </Button>
      </div>
    </div>
  );
}
