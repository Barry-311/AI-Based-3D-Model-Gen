import {
  Suspense,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
  type Dispatch,
  type ReactNode,
  type SetStateAction,
} from "react";
import * as THREE from "three";
import { GLTFLoader } from "three/addons/loaders/GLTFLoader.js";
import { GLTFExporter } from 'three/addons/exporters/GLTFExporter.js';
import { VertexNormalsHelper } from "three/addons/helpers/VertexNormalsHelper.js";
import { Canvas, useLoader, useThree } from "@react-three/fiber";
import { Bounds, Box, Html, OrbitControls, Text } from "@react-three/drei";
import { IconDownload, IconLoader2 } from "@tabler/icons-react";
import { ErrorBoundary } from "react-error-boundary";
import { Checkbox } from "./ui/checkbox";
import { Label } from "./ui/label";
import { Button } from "./ui/button";
import { Popover, PopoverContent, PopoverTrigger } from "./ui/popover";
import DownloadForm from "./DownloadForm";
import { toast } from "sonner";

type ModelDetails = {
  size: THREE.Vector3;
  center: THREE.Vector3;
};

type ExporterFunction = (filename: string) => Promise<void>;

interface IGLBModelProps {
  glbUrl: string;
  shouldRenderTexture: boolean;
  shouldRenderWireframe: boolean;
  shouldRenderVertextNormals: boolean;
  onLoad?: (details: ModelDetails) => void;
  onExportReady?: (exporters: {
    exportWithTextures: ExporterFunction;
    exportWithoutTextures: ExporterFunction;
  }) => void;
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

function GLBModel({
  glbUrl,
  shouldRenderTexture,
  shouldRenderWireframe,
  shouldRenderVertextNormals,
  onLoad,
  onExportReady
}: IGLBModelProps) {
  const gltf = useLoader(GLTFLoader, glbUrl);

  // 调整后的场景对象
  const [adjustedScene, setAdjustedScene] = useState<THREE.Group | null>(null);
  const [modelSize, setModelSize] = useState<THREE.Vector3 | null>(null);

  // 存储原始材质
  const originalMaterials = useRef(
    new Map<string, THREE.Material | THREE.Material[]>()
  );

  // 创建白色材质
  // PBR 模型使用 MeshStandardMaterial 效果更好
  const whiteMaterial = useMemo(
    () => new THREE.MeshStandardMaterial({ color: "white" }),
    []
  );

  // 创建法线辅助对象
  const normalsHelpers = useMemo(() => {
    // Always return an array
    if (!adjustedScene || !modelSize) return [];

    const normalLength = modelSize.length() * 0.01;

    const helpers: VertexNormalsHelper[] = [];
    adjustedScene.traverse((child) => {
      // Add a crucial check: ensure the mesh has a geometry with normal attributes
      if (
        child instanceof THREE.Mesh &&
        child.geometry &&
        child.geometry.attributes.normal
      ) {
        // If the mesh is valid, create a helper specifically for it
        helpers.push(new VertexNormalsHelper(child, normalLength, 0xff0000)); // Green color
      }
    });
    return helpers;
  }, [adjustedScene]);

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

      setModelSize(size);

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

    if (gltf.scene && onExportReady) {
      const exporter = new GLTFExporter();

      const createExporter = (removeTextures: boolean): ExporterFunction => {
        return async (filename: string) => {
          const sceneToExport = gltf.scene.clone(true);

          try {
            if (removeTextures) {
              sceneToExport.traverse((child) => {
                if (child instanceof THREE.Mesh && child.material) {
                  const whiteMaterial = new THREE.MeshStandardMaterial({ color: 0xffffff });
                  child.material = whiteMaterial;
                }
              });
            }

            const glb = await new Promise<ArrayBuffer>((resolve, reject) => {
              exporter.parse(
                sceneToExport,
                (result) => resolve(result as ArrayBuffer),
                (error) => reject(error),
                { binary: true }
              );
            });

            const blob = new Blob([glb], { type: 'model/gltf-binary' });
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = filename;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(link.href);
            
            toast.success("模型导出成功");
          } catch (error) {
            console.error("导出失败:", error);
            toast.error("模型导出失败");
          }
        };
      };

      onExportReady({
        exportWithTextures: createExporter(false),
        exportWithoutTextures: createExporter(true),
      });
    }

  }, [gltf.scene, onLoad]);

  // 根据 shouldRenderTexture prop 的变化来切换材质
  useEffect(() => {
    if (adjustedScene) {
      adjustedScene.traverse((child) => {
        if (child instanceof THREE.Mesh) {
          if (shouldRenderTexture) {
            // 恢复原始材质
            const originalMat = originalMaterials.current.get(child.uuid);
            if (originalMat) {
              child.material = originalMat;
            }
          } else {
            // 应用纯白材质，这里克隆一份以避免修改共享的材质实例
            child.material = whiteMaterial.clone();
          }

          // 独立应用线框属性
          // 无论当前是什么材质，都根据 shouldRenderWireframe 来设置 wireframe 属性
          if (Array.isArray(child.material)) {
            child.material.forEach(
              (mat) => (mat.wireframe = shouldRenderWireframe)
            );
          } else {
            // 确保 material 不是 undefined
            if (child.material) {
              child.material.wireframe = shouldRenderWireframe;
            }
          }
        }
      });
    }
  }, [
    adjustedScene,
    shouldRenderTexture,
    shouldRenderWireframe,
    whiteMaterial,
  ]);

  // 在 adjustedScene 准备好后才渲染
  return adjustedScene ? (
    <>
      <primitive object={adjustedScene} />
      {shouldRenderVertextNormals &&
        normalsHelpers.map((helper, index) => (
          <primitive key={index} object={helper} />
        ))}
    </>
  ) : null;
}

interface IModelPlaygroundProps {
  glbUrl: string;
  customControls?: ReactNode;
}

function ModelPlayground({ glbUrl, customControls }: IModelPlaygroundProps) {
  const [shouldRenderTexture, setShouldRenderTexture] = useState(true);
  const [shouldRenderWireframe, setShouldRenderWireframe] = useState(false);
  const [shouldRenderVertexNormals, setShouldRenderVertexNormals] =
    useState(false);
  const [modelDetails, setModelDetails] = useState<ModelDetails | null>(null);
  const [isContextLost, setIsContextLost] = useState(false);
  const [isInitialFit, setIsInitialFit] = useState(true); // 使 Bounds 只在模型初始加载时进行 fit 操作

  const groundSize = useMemo(() => {
    if (!modelDetails) return 40; // 默认大小
    // 取模型在 x 和 z 轴上尺寸的最大值，并乘以 1.5 作为留白
    const maxSize = Math.max(modelDetails.size.x, modelDetails.size.z);
    return Math.ceil(maxSize * 1.5);
  }, [modelDetails]);

  const handleModelLoad = useCallback((details: ModelDetails) => {
    // 在模型加载并首次适配相机后禁用 fit
    setModelDetails(details);
    setTimeout(() => setIsInitialFit(false), 2000);
  }, []);

  if (!glbUrl) {
    return (
      <div className="h-full w-full flex justify-center items-center">
        <div className="text-gray-400 select-none">无效的模型URL</div>
      </div>
    );
  }

  const modelExporters = useRef<{
    exportWithTextures: ExporterFunction | null;
    exportWithoutTextures: ExporterFunction | null;
  }>({
    exportWithTextures: null,
    exportWithoutTextures: null,
  });

  const handleExportReady = useCallback((exporters: {
    exportWithTextures: ExporterFunction;
    exportWithoutTextures: ExporterFunction;
  }) => {
    modelExporters.current = exporters;
  }, []);

  return (
    <>
      <div className="h-full w-full flex flex-col">
        <section className="mb-4 flex gap-5 items-center">
          <span className="flex gap-x-3 items-center">
            <Label htmlFor="render-texture">显示纹理</Label>
            <Checkbox
              id="render-texture"
              checked={shouldRenderTexture}
              onCheckedChange={(checked: boolean) =>
                setShouldRenderTexture(checked)
              }
            />
          </span>
          <span className="flex gap-x-3 items-center">
            <Label htmlFor="render-wireframe">显示线框</Label>
            <Checkbox
              id="render-wireframe"
              checked={shouldRenderWireframe}
              onCheckedChange={(checked: boolean) =>
                setShouldRenderWireframe(checked)
              }
            />
          </span>
          <span className="flex gap-x-3 items-center">
            <Label htmlFor="render-vertex-normals">显示顶点法线</Label>
            <Checkbox
              id="render-vertex-normals"
              checked={shouldRenderVertexNormals}
              onCheckedChange={(checked: boolean) =>
                setShouldRenderVertexNormals(checked)
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
                <DownloadForm exporters={modelExporters.current}  />
              </PopoverContent>
            </Popover>
          </span>
          {customControls && (
            <span className="ml-[auto] flex gap-x-2">{customControls}</span>
          )}
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
                    <IconLoader2 className="animate-spin w-xl h-xl" />
                  </Html>
                }
              >
                <Bounds fit={isInitialFit} clip observe margin={1.2}>
                  <GLBModel
                    // glbUrl="/test_models/city/cartoon_lowpoly_small_city_free_pack.glb"
                    // glbUrl="/test_models/cat/cat.glb"
                    // glbUrl="https://tripo-data.rg1.data.tripo3d.com/tcli_a288f685fd084ea3b13edb08445376e2/20250926/8e1a29df-9ef8-408a-ae17-0f5c1955a3ba/tripo_pbr_model_8e1a29df-9ef8-408a-ae17-0f5c1955a3ba.glb?Policy=eyJTdGF0ZW1lbnQiOlt7IlJlc291cmNlIjoiaHR0cHM6Ly90cmlwby1kYXRhLnJnMS5kYXRhLnRyaXBvM2QuY29tL3RjbGlfYTI4OGY2ODVmZDA4NGVhM2IxM2VkYjA4NDQ1Mzc2ZTIvMjAyNTA5MjYvOGUxYTI5ZGYtOWVmOC00MDhhLWFlMTctMGY1YzE5NTVhM2JhL3RyaXBvX3Bicl9tb2RlbF84ZTFhMjlkZi05ZWY4LTQwOGEtYWUxNy0wZjVjMTk1NWEzYmEuZ2xiIiwiQ29uZGl0aW9uIjp7IkRhdGVMZXNzVGhhbiI6eyJBV1M6RXBvY2hUaW1lIjoxNzU4OTMxMjAwfX19XX0_&Signature=Aw2wNxAi8QxFuiFoHvrSjfXF~mHvw00n7fT3NjIhaLBUujHhHey4hrcaHGoQYmYPltuamRrVayIUdNtvbILiR-k1tHPnhwOZKE~fQpv9szyfKLNDV3slqYfd6NpSHGkU-UChCunxPc5RJ4qrHPlQBsWJJm4ELPGzftgWEyRzHYjwiXh9sS4sS4a273KgnChRTvAGGaBTN6LGiJd~aYTTHXaCJDRqEbecH-wA0AUJd7nevvWoIRv5Yu818432ircxq-xydIE1Vu2km-Bl8K~TU3bqy2EQlCQQtSJ9ypQzvH6JDdyDmWikzDCgfptQIW-6e1n3AwfMZGtvr4ARue6mNQ__&Key-Pair-Id=K1676C64NMVM2J"
                    glbUrl={glbUrl || ""}
                    shouldRenderTexture={shouldRenderTexture}
                    shouldRenderWireframe={shouldRenderWireframe}
                    shouldRenderVertextNormals={shouldRenderVertexNormals}
                    onLoad={handleModelLoad}
                    onExportReady={handleExportReady}
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
      {/* )} */}
    </>
  );
}

export default ModelPlayground;
