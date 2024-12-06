const { mergeConfig } = require('vite');
const path = require('path');
const frontend = path.resolve(__dirname, '../src/main/frontend/');

module.exports = {
  stories: [`${frontend}/**/*.stories.@(js|jsx|mjs|ts|tsx|mdx)`],
  addons: [
    '@storybook/addon-docs',
    '@storybook/addon-links',
    '@storybook/addon-essentials',
    '@storybook/addon-mdx-gfm',
  ],
  framework: {
    name: '@storybook/vue3-vite',
    options: {},
  },
  async viteFinal(config) {
    return mergeConfig(config, {
      resolve: {
        alias: {
          '@': frontend,
        },
        extensions: ['.vue', '.js', '.json'],
      },
    });
  },
};
