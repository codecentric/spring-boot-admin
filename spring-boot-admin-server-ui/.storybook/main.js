const vueJsx = require('@vitejs/plugin-vue-jsx').default;
const { mergeConfig } = require('vite');

const path = require('path');
const frontend = path.resolve(__dirname, '../src/main/frontend/');
module.exports = {
  stories: [
    {
      directory: frontend,
    },
  ],
  addons: [
    '@storybook/addon-links',
    '@storybook/addon-essentials',
    '@storybook/addon-mdx-gfm',
  ],
  framework: {
    name: '@storybook/vue3-vite',
    options: {},
  },
  async viteFinal(config) {
    config.plugins = config.plugins.filter((p) => p.name !== 'vue-docgen');
    config.plugins.push(vueJsx());
    return mergeConfig(config, {
      resolve: {
        alias: {
          '@': frontend,
        },
        extensions: ['.vue', '.js', '.json'],
      },
    });
  },
  docs: {
    autodocs: true,
  },
};
