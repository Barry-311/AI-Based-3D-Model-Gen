import {
  Suspense,
  useEffect,
  useMemo,
  useRef,
  useState,
  type Dispatch,
  type SetStateAction,
} from "react";
import * as THREE from "three";
import { OBJLoader } from "three/addons/loaders/OBJLoader.js";
import { MTLLoader } from "three/addons/loaders/MTLLoader.js";
import { GLTFLoader } from "three/addons/loaders/GLTFLoader.js";
import { Canvas, useLoader, useThree } from "@react-three/fiber";
import { Bounds, Box, Html, OrbitControls, Text } from "@react-three/drei";
import { IconDownload } from "@tabler/icons-react";
import { ErrorBoundary } from "react-error-boundary";
import { Checkbox } from "./ui/checkbox";
import { Label } from "./ui/label";
import { Button } from "./ui/button";
import { Popover, PopoverContent, PopoverTrigger } from "./ui/popover";
import { Progress } from "./ui/progress";
import ModelIcon from "./ModelIcon";
import DownloadForm from "./DownloadForm";
import useGenerationStore from "@/stores/generationStore";
import { TaskStatus } from "@/types/generation";

type ModelDetails = {
  size: THREE.Vector3;
  center: THREE.Vector3;
};

interface IOBJModelProps {
  objUrl: string;
  mtlUrl: string;
  renderTexture: boolean;
}

interface IGLBModelProps {
  glbUrl: string;
  shouldRenderTexture: boolean;
  onLoad?: (details: ModelDetails) => void;
}

function GroundPlane({ size = 40 }) {
  const texture = useLoader(
    THREE.TextureLoader,
    "https://threejs.org/manual/examples/resources/images/checker.png"
  );
  texture.wrapS = THREE.RepeatWrapping;
  texture.wrapT = THREE.RepeatWrapping;
  texture.repeat.set(20, 20);
  texture.colorSpace = THREE.SRGBColorSpace;
  texture.magFilter = THREE.NearestFilter;

  return (
    <mesh rotation-x={-Math.PI / 2} receiveShadow>
      <planeGeometry args={[size, size]} />
      <meshPhongMaterial map={texture} side={THREE.DoubleSide} />
    </mesh>
  );
}

function ContextManager({
  setContextLost,
}: {
  setContextLost: Dispatch<SetStateAction<boolean>>;
}) {
  const { gl } = useThree();

  useEffect(() => {
    const handleContextLost = (event: Event) => {
      event.preventDefault();
      console.error("WebGL context lost!");
      setContextLost(true);
    };

    const handleContextRestored = () => {
      console.log("WebGL context restored.");
      setContextLost(false);
    };

    const canvas = gl.domElement;
    canvas.addEventListener("webglcontextlost", handleContextLost, false);
    canvas.addEventListener(
      "webglcontextrestored",
      handleContextRestored,
      false
    );

    return () => {
      canvas.removeEventListener("webglcontextlost", handleContextLost);
      canvas.removeEventListener("webglcontextrestored", handleContextRestored);
    };
  }, [gl, setContextLost]);

  return null;
}

function SceneUpdater({ modelDetails }: { modelDetails: ModelDetails | null }) {
  const { camera } = useThree();

  // 更新相机的 far 属性
  useEffect(() => {
    if (modelDetails) {
      const maxDim = Math.max(
        modelDetails.size.x,
        modelDetails.size.y,
        modelDetails.size.z
      );

      camera.far = maxDim * 2 + 100000;
      camera.updateProjectionMatrix();
    }
  }, [modelDetails, camera]);

  return null;
}

function ErrorFallback({ error }: { error: { message: string } }) {
  return (
    <mesh position-y={2}>
      <boxGeometry args={[3, 1, 0.1]} />
      <meshStandardMaterial color="red" />
      <Text
        position-z={0.1}
        fontSize={0.2}
        color="white"
        maxWidth={2.8}
        textAlign="center"
      >
        无法加载模型: {error.message}
      </Text>
    </mesh>
  );
}

function OBJModel({ objUrl, mtlUrl, renderTexture }: IOBJModelProps) {
  const materials = useLoader(MTLLoader, mtlUrl);
  const obj = useLoader(OBJLoader, objUrl, (loader) => {
    materials.preload();
    for (const material of Object.values(materials.materials)) {
      material.side = THREE.DoubleSide;
    }
    loader.setMaterials(materials);
  });

  if (!obj) {
    return null;
  }

  // 当 renderTexture 变化时，克隆一个新的模型并切换它的材质
  const clonedObj = useMemo(() => {
    const clone = obj.clone();
    clone.traverse((child) => {
      if (child instanceof THREE.Mesh) {
        if (!renderTexture) {
          // 如果不渲染纹理，就换上一个白色材质
          child.material = new THREE.MeshPhongMaterial({ color: "white" });
        }
      }
    });
    return clone;
  }, [obj, renderTexture]);

  // primitive 组件直接渲染一个现有的 Threejs Object3D 对象
  return <primitive object={clonedObj} />;
}

function GLBModel({ glbUrl, shouldRenderTexture, onLoad }: IGLBModelProps) {
  const gltf = useLoader(GLTFLoader, glbUrl);

  // 调整后的场景对象
  const [adjustedScene, setAdjustedScene] = useState<THREE.Group | null>(null);

  // 存储原始材质
  const originalMaterials = useRef(
    new Map<string, THREE.Material | THREE.Material[]>()
  );

  // 创建纯白材质
  // PBR 模型使用 MeshStandardMaterial 效果更好
  const whiteMaterial = useMemo(
    () => new THREE.MeshStandardMaterial({ color: "white" }),
    []
  );

  useEffect(() => {
    if (gltf.scene) {
      // 克隆场景，避免直接修改 useLoader 的缓存结果
      const sceneClone = gltf.scene.clone();

      // 在定位之前，先遍历并保存原始材质
      sceneClone.traverse((child) => {
        if (child instanceof THREE.Mesh) {
          // 使用 mesh 的 uuid 作为 key，material 作为 value
          originalMaterials.current.set(child.uuid, child.material);
        }
      });

      // 计算克隆场景的包围盒
      const box = new THREE.Box3().setFromObject(sceneClone);
      const size = new THREE.Vector3();
      const center = new THREE.Vector3();
      box.getSize(size);
      box.getCenter(center);

      // 计算垂直偏移量，将模型向上移动，使其底部位于 y=0
      const yOffset = -box.min.y;
      sceneClone.position.y = yOffset;

      // 保存调整好的场景
      setAdjustedScene(sceneClone);

      // 更新包围盒中心点信息再传出
      // 因为我们移动了模型，它的世界坐标中心也变了
      box.setFromObject(sceneClone);
      box.getCenter(center);

      // 通过回调将尺寸和新的中心点信息传出
      if (onLoad) {
        onLoad({ size, center });
      }
    }
  }, [gltf.scene, onLoad]);

  // 根据 shouldRenderTexture prop 的变化来切换材质
  useEffect(() => {
    if (adjustedScene) {
      adjustedScene.traverse((child) => {
        if (child instanceof THREE.Mesh) {
          if (shouldRenderTexture) {
            // 显示纹理：从 Map 中恢复原始材质
            child.material =
              originalMaterials.current.get(child.uuid) || child.material;
          } else {
            // 不显示纹理：应用纯白材质
            child.material = whiteMaterial;
          }
        }
      });
    }
  }, [adjustedScene, shouldRenderTexture, whiteMaterial]);

  // 在 adjustedScene 准备好后才渲染
  return adjustedScene ? <primitive object={adjustedScene} /> : null;
}

function ModelPlayground() {
  const [shouldRenderTexture, setShouldRenderTexture] = useState(true);
  const [modelDetails, setModelDetails] = useState<ModelDetails | null>(null);
  const [isContextLost, setIsContextLost] = useState(false);

  const { status, progress, error, pbrModelUrl, renderImageUrl } =
    useGenerationStore();

  const groundSize = useMemo(() => {
    if (!modelDetails) return 40; // 默认大小
    // 取模型在 x 和 z 轴上尺寸的最大值，并乘以 1.5 作为留白
    const maxSize = Math.max(modelDetails.size.x, modelDetails.size.z);
    return Math.ceil(maxSize * 1.5);
  }, [modelDetails]);

  return (
    <>
      {status !== TaskStatus.COMPLETED ? (
        <div className="h-full w-full flex justify-center items-center">
          {status === TaskStatus.IDLE && <div>开始生成模型</div>}
          {status === TaskStatus.RUNNING && (
            <div className="w-full flex flex-col items-center gap-10">
              <ModelIcon />
              <Progress value={progress} className="w-[60%]" />
              <span>正在生成...</span>
            </div>
          )}
          {status === TaskStatus.FAILED && <div>生成时发生错误</div>}
        </div>
      ) : (
        <div className="h-full w-full flex flex-col">
          <section className="mb-4 flex gap-5 items-center">
            <span className="flex gap-x-2 items-center">
              <Label htmlFor="render-texture">显示纹理</Label>
              <Checkbox
                id="render-texture"
                checked={shouldRenderTexture}
                onCheckedChange={(checked: boolean) =>
                  setShouldRenderTexture(checked)
                }
              />
            </span>
            <span className="ml-[auto] flex gap-x-2">
              <Popover>
                <PopoverTrigger>
                  <Button variant="outline">
                    <IconDownload />
                  </Button>
                </PopoverTrigger>
                <PopoverContent>
                  <DownloadForm />
                </PopoverContent>
              </Popover>
            </span>
          </section>
          <div className="w-full h-full relative gap-10 justify-center items-center text-6xl text-gray-400">
            {isContextLost && (
              <div className="absolute inset-0 flex flex-col justify-center items-center bg-white z-10">
                <p className="text-2xl text-gray-500">渲染时发生错误</p>
              </div>
            )}
            <Canvas camera={{ position: [0, 10, 20], fov: 45 }}>
              <color attach="background" args={["#eeeeee"]} />
              <hemisphereLight args={[0xb1e1ff, 0xb97a20, 2]} />
              <directionalLight
                position={[0, 10, 0]}
                target-position={[-5, 0, 0]}
                intensity={10}
              />
              <GroundPlane size={groundSize} />
              <ErrorBoundary FallbackComponent={ErrorFallback}>
                <Suspense
                  fallback={
                    <Html center>
                      {/* <ModelIcon /> */}
                      <span>正在加载模型...</span>
                    </Html>
                  }
                >
                  {/* <Model
                    objUrl="https://threejs.org/manual/examples/resources/models/windmill/windmill.obj"
                    mtlUrl="https://threejs.org/manual/examples/resources/models/windmill/windmill.mtl"
                    // objUrl="/test_models/windmill/windmill.obj"
                    // mtlUrl="/test_models/windmill/windmill.mtl"
                    renderTexture={renderTexture}
                  /> */}
                  <Bounds fit clip observe margin={1.2}>
                    <GLBModel
                      // glbUrl="/test_models/city/cartoon_lowpoly_small_city_free_pack.glb"
                      // glbUrl="/test_models/cat/cat.glb"
                      glbUrl={pbrModelUrl || ""}
                      shouldRenderTexture={shouldRenderTexture}
                      onLoad={setModelDetails}
                    />
                    {modelDetails && (
                      // 隐形锚点，用于将 Bounds 的焦点拉低
                      <Box
                        position={[
                          modelDetails.center.x,
                          -modelDetails.size.y * 2, // 放在负距离处
                          modelDetails.center.z,
                        ]}
                        args={[1, 1, 1]}
                      >
                        <meshBasicMaterial transparent opacity={0} />
                      </Box>
                    )}
                  </Bounds>
                </Suspense>
              </ErrorBoundary>
              <OrbitControls makeDefault />
              <SceneUpdater modelDetails={modelDetails} />
              <ContextManager setContextLost={setIsContextLost} />
            </Canvas>
          </div>
        </div>
      )}
    </>
  );
}

export default ModelPlayground;
