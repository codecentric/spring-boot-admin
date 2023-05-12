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
