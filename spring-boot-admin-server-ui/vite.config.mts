/// <reference types="vitest" />
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';
import { visualizer } from 'rollup-plugin-visualizer';
import { defineConfig, loadEnv } from 'vite';
import { viteStaticCopy } from 'vite-plugin-static-copy';

import postcss from './postcss.config';

const frontendDir = resolve(__dirname, 'src/main/frontend');
const outDir = resolve(__dirname, 'target/dist');

export default defineConfig(({ mode }) => {
  process.env = { ...process.env, ...loadEnv(mode, process.cwd()) };

  return {
    base: './',
    define: {
      __VUE_PROD_DEVTOOLS__: process.env.NODE_ENV === 'development',
      __PROJECT_VERSION__: JSON.stringify(
        `${process.env.PROJECT_VERSION || '0.0.0'}`,
      ),
    },
    plugins: [
      vue(),
      visualizer(() => {
        return {
          filename: resolve(__dirname, 'target/vite.bundle-size-analyzer.html'),
        };
      }),
      viteStaticCopy({
        targets: [
          {
            src: ['sba-settings.js', 'assets/'],
            dest: outDir,
          },
        ],
      }),
    ],
    css: {
      postcss,
      preprocessorOptions: {
        scss: {
          api: 'modern-compiler',
        },
      },
    },
    test: {
      root: __dirname,
      globals: true,
      environment: 'jsdom',
      setupFiles: [resolve(frontendDir, 'tests/setup.ts')],
      include: [
        resolve(
          frontendDir,
          '**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}',
        ),
      ],
    },
    root: frontendDir,
    build: {
      target: 'es2020',
      outDir,
      rollupOptions: {
        input: {
          sba: resolve(frontendDir, './index.html'),
          login: resolve(frontendDir, './login.html'),
        },
        external: ['sba-settings.js', 'public/variables.css'],
      },
    },
    resolve: {
      alias: {
        '@': frontendDir,
      },
      extensions: ['.vue', '.js', '.json', '.ts'],
    },
    server: {
      proxy: {
        '^(/sba-settings.js|/variables.css)': {
          target: 'http://localhost:8080',
          changeOrigin: true,
        },
        '^/(applications|instances|notifications/|extensions/)': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          bypass: (req) => {
            const isEventStream = req.headers.accept === 'text/event-stream';
            const isAjaxCall =
              req.headers['x-requested-with'] === 'XMLHttpRequest';
            const isFile = req.url.indexOf('.js') !== -1;
            const redirectToIndex = !(isAjaxCall || isEventStream) && !isFile;
            if (redirectToIndex) {
              return '/index.html';
            }
          },
        },
      },
    },
  };
});
