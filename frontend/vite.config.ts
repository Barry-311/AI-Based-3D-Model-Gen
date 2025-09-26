import path from "path";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { mockDevServerPlugin } from "vite-plugin-mock-dev-server";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss(), mockDevServerPlugin()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    proxy: {
      // "/api": {
      //   target: "https://api.tripo3d.ai",
      //   changeOrigin: true,
      //   rewrite: (path) => path.replace(/^\/api/, ""),
      // },
      "/generate-stream": {
        target: "http://localhost:5173",
        changeOrigin: true,
      },
      "/generate-stream-actual": {
        target: "http://localhost:5173",
        changeOrigin: true,
      },
      "/generate-stream-image": {
        target: "http://localhost:5173",
        changeOrigin: true,
      },
      "/generate-stream-image-actual": {
        target: "http://localhost:5173",
        changeOrigin: true,
      },
      "/api/user/register": {
        target: "http://localhost:5173",
        changeOrigin: true,
      },
      "/api/user/login": {
        target: "http://localhost:5173",
        changeOrigin: true,
      },
      "/api/user/logout": {
        target: "http://localhost:5173",
        changeOrigin: true,
      },
    },
  },
});
