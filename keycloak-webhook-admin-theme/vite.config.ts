import path from "node:path";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";

const themeResourcesDir = path.resolve(
  __dirname,
  "build/theme/keycloak-webhook/admin/resources",
);

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: themeResourcesDir,
    emptyOutDir: true,
    manifest: true,
    rollupOptions: {
      input: path.resolve(__dirname, "src/main.tsx"),
    },
  },
});
