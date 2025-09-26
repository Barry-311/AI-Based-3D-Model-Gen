import { useState } from "react";
import { LoginForm } from "./LoginForm";
import { Button } from "./ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "./ui/dialog";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuPortal,
  DropdownMenuSeparator,
  DropdownMenuShortcut,
  DropdownMenuSub,
  DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuTrigger,
} from "./ui/dropdown-menu";
import { RegisterForm } from "./RegisterForm";
import useUserStore from "@/stores/userStore";
import { IconUser } from "@tabler/icons-react";

function Header() {
  const [dialogView, setDialogView] = useState<"login" | "register">("login");
  const [isOpen, setIsOpen] = useState(false);

  const { isAuthenticated, user, logout } = useUserStore();

  const handleOpenChange = (open: boolean) => {
    setIsOpen(open);
    if (!open) {
      setTimeout(() => {
        setDialogView("login");
      }, 200);
    }
  };

  return (
    <div className="w-full flex items-center gap-x-5 underline-offset-8">
      <span>🔥LOGO🔥</span>
      <nav className="flex-1">
        <ul className="flex w-full gap-x-10 items-center">
          <li className="underline underline-offset-8">
            <a href="">首页</a>
          </li>
          <li>
            <a href="">社区</a>
          </li>
          <li className="ml-[auto]">
            {isAuthenticated ? (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="link">
                    {user?.userAccount}
                    <IconUser />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <DropdownMenuItem onClick={logout}>退出登录</DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <Dialog open={isOpen} onOpenChange={handleOpenChange}>
                <DialogTrigger asChild>
                  <Button>登录</Button>
                </DialogTrigger>
                <DialogContent className="sm:max-w-[425px]">
                  <DialogHeader>
                    <DialogTitle>
                      {dialogView === "login" ? "登录" : "创建账户"}
                    </DialogTitle>
                    <DialogDescription className="sr-only">
                      {dialogView === "login" ? "登录表单" : "注册表单"}
                    </DialogDescription>
                  </DialogHeader>
                  {dialogView === "login" ? (
                    <LoginForm
                      onSwitchToRegister={() => setDialogView("register")}
                    />
                  ) : (
                    <RegisterForm
                      onSwitchToLogin={() => setDialogView("login")}
                    />
                  )}
                </DialogContent>
              </Dialog>
            )}
          </li>
        </ul>
      </nav>
    </div>
  );
}

export default Header;
