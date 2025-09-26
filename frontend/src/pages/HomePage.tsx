import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import ModelPlayground from "@/components/ModelPlayground";
import PromptCard from "@/components/PromptCard";

function HomePage() {
  return (
    <>
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
    </>
  );
}

export default HomePage;
