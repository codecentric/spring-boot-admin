const path = require('path');

module.exports = {
  "stories": [
    "../src/**/*.stories.mdx",
    "../src/**/*.stories.@(js|jsx|ts|tsx)"
  ],
  "addons": [
    "@storybook/addon-links",
    "@storybook/addon-essentials",
    "@storybook/addon-postcss",
    "@storybook/preset-scss"
  ],
  webpackFinal: async (config, { configType }) => {
    config.resolve = {
      alias: {
        'vue$': 'vue/dist/vue.esm.js',
        "@": path.resolve(__dirname, '../src/main/frontend'),
      },
      extensions: ['.ts', '.js', '.vue'],
    }
    return config;
  },
}
