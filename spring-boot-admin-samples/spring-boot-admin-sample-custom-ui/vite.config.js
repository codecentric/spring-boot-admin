import vue from "@vitejs/plugin-vue";
import path from "path";
import { defineConfig } from "vite";
import { viteStaticCopy } from "vite-plugin-static-copy";

export default defineConfig({
  plugins: [
    vue(),
    viteStaticCopy({
      targets: [
        {
          src: "src/routes.txt",
          dest: "./",
        },
      ],
    }),
  ],
  build: {
    target: "es2015",
    sourcemap: true,
    minify: false,
    outDir: "target/dist",
    lib: {
      entry: path.resolve(__dirname, "src/index.js"),
      name: "CustomUi",
      formats: ["umd"],
      fileName: () => "custom-ui.js",
    },
    define: {
      __VUE_PROD_DEVTOOLS__: process.env.NODE_ENV === "development",
    },
    rollupOptions: {
      external: ["vue"],
      output: {
        globals: {
          vue: "Vue",
        },
      },
    },
  },
});
