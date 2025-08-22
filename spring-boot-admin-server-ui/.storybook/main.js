const { mergeConfig } = require('vite');
const path = require('path');
const frontend = path.resolve(__dirname, '../src/main/frontend/');
module.exports = {
  stories: ['../src/main/frontend/**/*.stories.@(js|jsx|ts|tsx|mdx)'],
  addons: ['@storybook/addon-links', '@storybook/addon-docs'],

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
  }
};
