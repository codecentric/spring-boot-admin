import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';
import visualizer from 'rollup-plugin-visualizer';
import { fileURLToPath } from 'url';
import { defineConfig, loadEnv } from 'vite';
import { viteStaticCopy } from 'vite-plugin-static-copy';

import postcss from './postcss.config';

const root = fileURLToPath(new URL('./src/main/frontend/', import.meta.url));
const outDir = fileURLToPath(new URL('./target/dist/', import.meta.url));

export default defineConfig(({ mode }) => {
  process.env = { ...process.env, ...loadEnv(mode, process.cwd()) };

  return {
    root,
    base: '',
    define: {
      __PROJECT_VERSION__: JSON.stringify(
        `${process.env.PROJECT_VERSION || '0.0.0'}`
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
    },
    build: {
      target: 'es2015',
      sourcemap: true,
      outDir,
      rollupOptions: {
        input: {
          sba: resolve(root, './index.html'),
          login: resolve(root, './login.html'),
        },
        external: ['sba-settings.js', 'public/variables.css'],
      },
    },
    resolve: {
      alias: {
        '@': root,
      },
      extensions: ['.vue', '.js', '.json'],
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
