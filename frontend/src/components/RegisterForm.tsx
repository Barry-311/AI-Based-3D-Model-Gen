import z from "zod";
import { Button } from "./ui/button";
import BaseForm from "./BaseForm";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "./ui/form";
import { Input } from "./ui/input";
import { register } from "@/api/userApi";
import { toast } from "sonner";

interface RegisterFormProps {
  onSwitchToLogin: () => void;
}

const registerSchema = z
  .object({
    username: z.string().min(1, { message: "用户名不能为空" }),
    password: z.string().min(6, { message: "密码至少需要 6 位" }),
    checkPassword: z.string().min(6, { message: "密码至少需要 6 位" }),
  })
  .refine((data) => data.password === data.checkPassword, {
    message: "两次输入的密码不一致",
    path: ["checkPassword"],
  });

export function RegisterForm({ onSwitchToLogin }: RegisterFormProps) {
  async function handleSubmit(data: z.infer<typeof registerSchema>) {
    const reqData = {
      userAccount: data.username,
      userPassword: data.password,
      checkPassword: data.checkPassword,
    };

    try {
      const response = await register(reqData);
      toast.success(response.message || "注册成功！");
      onSwitchToLogin(); // 注册成功后自动切换到登录表单
    } catch (error) {
      if (error instanceof Error) {
        toast.error(error.message);
      } else {
        toast.error("发生未知错误");
      }
    }
  }
  return (
    <div className="grid gap-4 py-4">
      <BaseForm
        schema={registerSchema}
        onSubmit={handleSubmit}
        submitButtonText={{
          default: "注册",
          submitting: "注册中...",
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
            <FormField
              control={form.control}
              name="checkPassword"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>确认密码</FormLabel>
                  <FormControl>
                    <Input
                      type="password"
                      placeholder="请再次输入密码"
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
      <p className="text-center text-sm">
        已有账号？
        <Button variant="link" onClick={onSwitchToLogin}>
          立即登录
        </Button>
      </p>
    </div>
  );
}
