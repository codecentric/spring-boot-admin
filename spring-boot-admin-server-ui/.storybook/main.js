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
    {
      name: '@storybook/addon-postcss',
      options: {
        postcssLoaderOptions: {
          implementation: require('postcss'),
        },
      },
    },
  ],
  framework: '@storybook/vue3',
  core: {
    builder: '@storybook/builder-vite',
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
};
