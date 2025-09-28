import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { IconUser } from "@tabler/icons-react";
import { motion } from "framer-motion";
import { LoginForm } from "./LoginForm";
import { Button } from "./ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "./ui/dialog";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "./ui/dropdown-menu";
import { RegisterForm } from "./RegisterForm";
import useUserStore from "@/stores/userStore";
import { cn } from "@/lib/utils";
import { FeedbackForm } from "./FeedbackForm";
import FeedbackList from "./FeedbackList";

const navItems = [
  { name: "首页", href: "/" },
  { name: "模型库", href: "/model-library" },
];

function Header() {
  const [dialogView, setDialogView] = useState<"login" | "register">("login");
  const [isOpen, setIsOpen] = useState(false);
  const { isAuthenticated, user, logout } = useUserStore();
  const location = useLocation();
  const [activeTab, setActiveTab] = useState(location.pathname);

  const handleOpenChange = (open: boolean) => {
    setIsOpen(open);
    if (!open) {
      setTimeout(() => {
        setDialogView("login");
      }, 200);
    }
  };

  return (
    <div className="w-full flex items-center gap-x-10 underline-offset-8">
      <span className="select-none">🔥3DMGF</span>
      <nav className="flex-1">
        <ul className="flex w-full gap-x-10 items-center">
          {navItems.map((item) => (
            <motion.li key={item.href} className="relative select-none">
              <Link
                to={item.href}
                onClick={() => setActiveTab(item.href)}
                className={cn(
                  "block px-1",
                  activeTab === item.href
                    ? "text-primary"
                    : "text-foreground/60"
                )}
              >
                {item.name}
              </Link>
              {activeTab === item.href && (
                <motion.div
                  layoutId="underline"
                  className="absolute bottom-[-4px] left-0 right-0 h-[1px] bg-primary"
                  transition={{ type: "spring", stiffness: 350, damping: 30 }}
                />
              )}
            </motion.li>
          ))}
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
                  <Dialog open={isOpen} onOpenChange={handleOpenChange}>
                    <DialogTrigger asChild>
                      <DropdownMenuItem onSelect={(e) => e.preventDefault()}>
                        用户反馈
                      </DropdownMenuItem>
                    </DialogTrigger>
                    <DialogContent  className="max-w-fit">
                      {user?.userRole === "admin" ? (
                        <FeedbackList />
                      ) : (
                        <FeedbackForm />
                      )}
                    </DialogContent>
                  </Dialog>
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
