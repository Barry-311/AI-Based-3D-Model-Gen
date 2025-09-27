import { useCallback, useEffect, useRef } from "react";
import ModelCard from "@/components/ModelCard";
import { useModelStore } from "@/stores/useModelStore";
import { IconLoader2 } from "@tabler/icons-react";

function LoadingSpinner() {
  return (
    <div className="flex justify-center items-center py-2">
      <IconLoader2 className="animate-spin" />
      <span>正在加载...</span>
    </div>
  );
}

function ModelLibraryPage() {
  const { models, fetchModels, hasMore, isLoading, reset } = useModelStore();

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

  // useEffect(() => {
  //   // 组件首次加载或依赖项变化时，重置并加载第一页
  //   reset();
  //   fetchModels();
  // }, [fetchModels, reset]);

  useEffect(() => {
    // 仅在 model 列表为空时加载初始数据，防止热更新等情况下的重复加载
    if (useModelStore.getState().models.length === 0) {
      fetchModels();
    }
  }, []);

  return (
    <div className="w-full p-5 grid xl:grid-cols-[repeeat(auto-fill,_500px)] lg:grid-cols-3 md:grid-cols-2 grid-cols-1 justify-center justify-items-start gap-5">
      {models.map((model, index) => {
        if (models.length === index + 1) {
          return (
            <div ref={lastElementRef} key={model.id}>
              <ModelCard
                glbUrl={model.pbrModelUrl}
                renderImage={model.renderedImageUrl}
                prompt={model.prompt}
                creater={`User ${model.userId}`}
                createTime={model.createTime}
              />
              {/* <ModelCard
                glbUrl="/test_models/dog/dog.glb"
                renderImage="https://picsum.photos/500/500"
                prompt="提示词提示词"
                creater="user123"
                createTime="2025-01-01"
              /> */}
            </div>
          );
        } else {
          return (
            <ModelCard
              key={index}
              glbUrl={model.pbrModelUrl}
              renderImage={model.renderedImageUrl}
              prompt={model.prompt}
              creater={`User ${model.userId}`}
              createTime={model.createTime}
            />
            // <ModelCard
            //   key={index}
            //   glbUrl="/test_models/dog/dog.glb"
            //   renderImage="https://picsum.photos/500/500"
            //   prompt="提示词提示词"
            //   creater="user123"
            //   createTime="2025-01-01"
            // />
          );
        }
      })}

      <div className="w-full flex justify-center items-center pt-8 pb-4">
        {isLoading && <LoadingSpinner />}
        {!isLoading && !hasMore && models.length > 0 && (
          <div className="text-center text-gray-500">
            —&nbsp;没有更多数据了&nbsp;—
          </div>
        )}
      </div>
    </div>
  );
}

export default ModelLibraryPage;
