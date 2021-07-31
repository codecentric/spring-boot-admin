import "@/assets/css/base.scss";

import Vue from "vue/dist/vue.js";
import components from "@/components";

Vue.use(components);

export const parameters = {
  actions: { argTypesRegex: "^on[A-Z].*" },
  controls: {
    matchers: {
      color: /(background|color)$/i,
      date: /Date$/,
    },
  },
}
