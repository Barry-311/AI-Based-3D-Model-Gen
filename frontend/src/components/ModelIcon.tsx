function ModelIcon() {
  const CUBE_SIZE = 60;
  const HALF_SIZE = CUBE_SIZE / 2;

  const faceBaseClass = "absolute w-full h-full border border-[#99a1af]";

  return (
    <div className="flex items-center justify-center [perspective:1000px]">
      <div
        className="relative transform-3d animate-rotate-cube"
        style={{ width: `${CUBE_SIZE}px`, height: `${CUBE_SIZE}px` }}
      >
        <div
          className={faceBaseClass}
          style={{ transform: `rotateY(0deg) translateZ(${HALF_SIZE}px)` }}
        ></div>
        <div
          className={faceBaseClass}
          style={{ transform: `rotateY(180deg) translateZ(${HALF_SIZE}px)` }}
        ></div>
        <div
          className={faceBaseClass}
          style={{ transform: `rotateX(90deg) translateZ(${HALF_SIZE}px)` }}
        ></div>
        <div
          className={faceBaseClass}
          style={{ transform: `rotateX(-90deg) translateZ(${HALF_SIZE}px)` }}
        ></div>
        <div
          className={faceBaseClass}
          style={{ transform: `rotateY(90deg) translateZ(${HALF_SIZE}px)` }}
        ></div>
        <div
          className={faceBaseClass}
          style={{ transform: `rotateY(-90deg) translateZ(${HALF_SIZE}px)` }}
        ></div>
      </div>
    </div>
  );
}

export default ModelIcon;
