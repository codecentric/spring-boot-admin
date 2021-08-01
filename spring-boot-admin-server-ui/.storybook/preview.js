import "@/assets/css/base.scss";

import Vue from "vue/dist/vue.js";
import components from "@/components";
import i18n from "@/i18n";
import VueI18n from "vue-i18n";

Vue.use(VueI18n);
Vue.use(components);

export const parameters = {
  actions: {argTypesRegex: "^on[A-Z].*"},
  controls: {
    matchers: {
      color: /(background|color)$/i,
      date: /Date$/,
    },
  },
}

export const decorators = [
  (story, context) => {
    const wrapped = story(context)
    return Vue.extend({
      i18n,
      components: {wrapped},
      template: `<wrapped/>`
    })
  },
]
