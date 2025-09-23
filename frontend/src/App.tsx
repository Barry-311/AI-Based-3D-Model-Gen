import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import Header from "./components/Header";
import ModelPlayground from "./components/ModelPlayground";
import PromptCard from "./components/PromptCard";

function App() {
  return (
    <div className="flex flex-col h-[100vh]">
      <header className="flex h-5 pl-5 pr-5 mt-5 mb-2 gap-x-10">
        <Header />
      </header>
      <main className="flex flex-col md:flex-row flex-1 p-5 gap-5">
        <section className="basis-[30%]">
          <Card className="flex-1">
            <CardHeader>
              <CardTitle>新建模型</CardTitle>
            </CardHeader>
            <CardContent>
              <PromptCard />
            </CardContent>
          </Card>
        </section>
        <section className="basis-[70%] flex">
          <Card className="flex-1 flex flex-col">
            <CardContent className="flex-1">
              <ModelPlayground />
            </CardContent>
          </Card>
        </section>
      </main>
    </div>
  );
}

export default App;
