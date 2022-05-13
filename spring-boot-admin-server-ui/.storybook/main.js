const {mergeConfig} = require("vite");
const {resolve} = require("path");

module.exports = {
  "stories": [
    "../src/main/frontend/components/*.stories.mdx",
    "../src/main/frontend/components/*.stories.@(js|jsx|ts|tsx)",
    "../src/main/frontend/views/**/*.stories.mdx",
    "../src/main/frontend/views/**/*.stories.@(js|jsx|ts|tsx)",
    "../src/main/frontend/**/*.stories.@(js|jsx|ts|tsx)"
  ],
  "addons": [
    "@storybook/addon-links",
    "@storybook/addon-essentials",
    {
      name: '@storybook/addon-postcss',
      options: {
        postcssLoaderOptions: {
          implementation: require('postcss'),
        },
      },
    },
  ],
  "framework": "@storybook/vue3",
  "core": {
    "builder": "@storybook/builder-vite"
  },
  async viteFinal(config) {
    const frontend = resolve(__dirname, '../src/main/frontend');
    config.plugins = config.plugins.filter(p => p.name !== "vue-docgen");
    return mergeConfig(config, {
      resolve: {
        alias: {
          '@': frontend,
        },
      },
    });
  },
};
