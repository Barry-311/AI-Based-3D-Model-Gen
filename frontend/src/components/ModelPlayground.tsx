import { Suspense, useMemo, useState } from "react";
import * as THREE from "three";
import { OBJLoader } from "three/addons/loaders/OBJLoader.js";
import { MTLLoader } from "three/addons/loaders/MTLLoader.js";
import { Canvas, useLoader } from "@react-three/fiber";
import { OrbitControls } from "@react-three/drei";
import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "./ui/label";
import ModelIcon from "./ModelIcon";

interface IModel {
  mtlUrl: string;
  objUrl: string;
  renderTexture: boolean;
}

function GroundPlane() {
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
      <planeGeometry args={[40, 40]} />
      <meshPhongMaterial map={texture} side={THREE.DoubleSide} />
    </mesh>
  );
}

function Model({ objUrl, mtlUrl, renderTexture }: IModel) {
  const materials = useLoader(MTLLoader, objUrl);
  const obj = useLoader(
    OBJLoader,
    mtlUrl,
    (loader) => {
      materials.preload();
      for (const material of Object.values(materials.materials)) {
        material.side = THREE.DoubleSide;
      }
      loader.setMaterials(materials);
    }
  );

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

function ModelPlayground() {
  const [renderTexture, setRenderTexture] = useState(true);

  return (
    <div className="h-full w-full flex flex-col">
      <section className="mb-4">
        <span className="flex gap-x-2">
          <Label htmlFor="render-texture">显示纹理</Label>
          <Checkbox
            id="render-texture"
            checked={renderTexture}
            onCheckedChange={(checked: boolean) => setRenderTexture(checked)}
          />
        </span>
      </section>
      <div className="w-full h-full flex gap-10 justify-center items-center text-6xl text-gray-400">
        {/* <ModelIcon />
        模型预览 */}
        <Canvas camera={{ position: [0, 10, 20], fov: 45 }}>
          <color attach="background" args={["#eeeeee"]} />
          <hemisphereLight args={[0xb1e1ff, 0xb97a20, 2]} />
          <directionalLight
            position={[0, 10, 0]}
            target-position={[-5, 0, 0]}
            intensity={10}
          />
          <GroundPlane />
          <Suspense fallback={<ModelIcon />}>
            <Model
              objUrl="https://threejs.org/manual/examples/resources/models/windmill/windmill.mtl" // "/windmill/windmill.mtl"
              mtlUrl="https://threejs.org/manual/examples/resources/models/windmill/windmill.obj" // "/windmill/windmill.obj"
              renderTexture={renderTexture}
            />
          </Suspense>
          <OrbitControls target={[0, 5, 0]} />
        </Canvas>
      </div>
    </div>
  );
}

export default ModelPlayground;
