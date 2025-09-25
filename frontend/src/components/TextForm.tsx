import { Textarea } from "@/components/ui/textarea";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { z } from "zod";
import BaseForm from "./BaseForm";
import useGenerationStore from "@/stores/generationStore";

function TextForm() {
  const formSchema = z.object({
    prompt: z
      .string({
        error: "提示词不能为空",
      })
      .min(1, {
        message: "提示词不能为空",
      }),
  });

  const startGeneration = useGenerationStore((state) => state.startGeneration);
  const reset = useGenerationStore((state) => state.reset);

  async function handleSubmit(values: z.infer<typeof formSchema>) {
    startGeneration(values.prompt);
  }

  return (
    <BaseForm
      onSubmit={handleSubmit}
      schema={formSchema}
      submitButtonText={{ default: "生成", submitting: "正在生成..." }}
    >
      {(form) => (
        <FormField
          control={form.control}
          name="prompt"
          render={({ field }) => (
            <FormItem>
              <FormLabel>提示词</FormLabel>
              <FormControl>
                <Textarea
                  placeholder="描述你想要生成的对象"
                  className="flex-1"
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
      )}
    </BaseForm>
  );
}

export default TextForm;
