import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import ModelCard from "@/components/ModelCard";
import { useModelStore } from "@/stores/useModelStore";
import { IconLoader2 } from "@tabler/icons-react";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group";
import { Toggle } from "@/components/ui/toggle";

interface IFilterProps {
  sortOrder: "descend" | "ascend";
  onSortChange: (value: "descend" | "ascend") => void;
  filterTypes: string[];
  onFilterChange: (value: string) => void;
}

function LoadingSpinner() {
  return (
    <div className="flex justify-center items-center py-2">
      <IconLoader2 className="animate-spin" />
      <span>正在加载...</span>
    </div>
  );
}

function Filter({
  sortOrder,
  onSortChange,
  filterTypes,
  onFilterChange,
}: IFilterProps) {
  return (
    <section className="w-full flex gap-x-5 p-5">
      <Select value={sortOrder} onValueChange={onSortChange}>
        <SelectTrigger className="w-[150px] hover:cursor-pointer">
          <SelectValue placeholder="排序方式" />
        </SelectTrigger>
        <SelectContent>
          <SelectGroup>
            <SelectLabel>排序</SelectLabel>
            <SelectItem value="descend">最近发布</SelectItem>
            <SelectItem value="ascend">最早发布</SelectItem>
          </SelectGroup>
        </SelectContent>
      </Select>
      <div className="flex items-center gap-x-3">
        <Toggle
          variant="outline"
          className="rounded-full p-3 hover:cursor-pointer hover:shadow-lg"
          pressed={filterTypes.includes("text")}
          onPressedChange={() => onFilterChange("text")}
        >
          由文本提示词生成
        </Toggle>
        <Toggle
          variant="outline"
          className="rounded-full p-3 hover:cursor-pointer hover:shadow-lg"
          pressed={filterTypes.includes("image")}
          onPressedChange={() => onFilterChange("image")}
        >
          由参考图生成
        </Toggle>
      </div>
    </section>
  );
}

function ModelLibraryPage() {
  const {
    models,
    fetchModels,
    hasMore,
    isLoading,
    reset,
    sortOrder,
    setSortOrder,
  } = useModelStore();

  const [filterTypes, setFilterTypes] = useState<string[]>(["text", "image"]);

  const observer = useRef<IntersectionObserver | null>(null);

  const lastElementRef = useCallback(
    (node: HTMLDivElement | null) => {
      if (isLoading) return;

      if (observer.current) observer.current.disconnect();

      observer.current = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasMore) {
          fetchModels();
        }
      });

      if (node) observer.current.observe(node);
    },
    [isLoading, hasMore, fetchModels]
  );

  useEffect(() => {
    if (models.length === 0 && hasMore) {
      fetchModels();
    }
  }, [models.length, hasMore, fetchModels]);

  const handleSortChange = (value: "descend" | "ascend") => {
    if (value === sortOrder) return;
    setSortOrder(value);
    reset();
  };

  const handleFilterChange = (type: string) => {
    setFilterTypes(
      (prev) =>
        prev.includes(type)
          ? prev.filter((t) => t !== type) // 如果已存在，则移除
          : [...prev, type] // 如果不存在，则添加
    );
  };

  const filteredModels = useMemo(() => {
    const showText = filterTypes.includes("text");
    const showImage = filterTypes.includes("image");

    if (!showText && !showImage) {
      return [];
    }

    if (showText && showImage) {
      return models;
    }

    return models.filter((model) => {
      if (showText) {
        return !!model.prompt;
      }
      if (showImage) {
        return !!model.pictureUrl;
      }
      return false;
    });
  }, [models, filterTypes]);

  return (
    <div className="flex flex-col">
      <Filter
        sortOrder={sortOrder}
        onSortChange={handleSortChange}
        filterTypes={filterTypes}
        onFilterChange={handleFilterChange}
      />
      <div className="w-full p-5 grid xl:grid-cols-[repeeat(auto-fill,_500px)] lg:grid-cols-3 md:grid-cols-2 grid-cols-1 justify-center justify-items-start gap-5">
        {filteredModels.map((model, index) => {
          if (filteredModels.length === index + 1) {
            return (
              <div ref={lastElementRef} key={model.id}>
                <ModelCard
                  glbUrl={model.pbrModelUrl}
                  renderImage={model.renderedImageUrl}
                  prompt={model.prompt}
                  creater={`User ${model.userId}`}
                  createTime={model.createTime}
                />
              </div>
            );
          } else {
            return (
              <ModelCard
                key={model.id}
                glbUrl={model.pbrModelUrl}
                renderImage={model.renderedImageUrl}
                prompt={model.prompt}
                creater={`User ${model.userId}`}
                createTime={model.createTime}
              />
            );
          }
        })}

        <div className="w-full flex justify-center items-center pt-8 pb-4 col-span-full">
          {isLoading && <LoadingSpinner />}
          {!isLoading && !hasMore && filteredModels.length > 0 && (
            <div className="text-center text-gray-500">
              —&nbsp;没有更多数据了&nbsp;—
            </div>
          )}
          {!isLoading && filteredModels.length === 0 && models.length > 0 && (
            <div className="text-center text-gray-500">
              —&nbsp;没有符合筛选条件的模型&nbsp;—
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default ModelLibraryPage;
