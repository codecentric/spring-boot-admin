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
    const removedPlugins = [
      'vue-docgen',
      'vite-plugin-static-copy:build',
      'vite-plugin-static-copy:serve',
    ];
    config.plugins = config.plugins
      .flat(Infinity)
      .filter((p) => p && !removedPlugins.includes(p.name));
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
