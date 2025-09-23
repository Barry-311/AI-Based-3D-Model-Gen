import { Button } from "./ui/button";

function Header() {
  return (
    <>
      <span>🔥LOGO🔥</span>
      <nav className="flex-1">
        <ul className="flex w-full gap-x-10">
          <li className="underline underline-offset-8">
            <a href="">首页</a>
          </li>
          <li>
            <a href="">社区</a>
          </li>
          <li className="ml-[auto]">
            <Button>登录</Button>
          </li>
        </ul>
      </nav>
    </>
  );
}

export default Header;
