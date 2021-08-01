import {render as tlRender} from '@testing-library/vue';
import VueI18n from 'vue-i18n';
import components from '@/components';
import Vue from 'vue';
import i18n from '@/i18n';

Vue.use(VueI18n);
Vue.use(components);

export const render = (testComponent, options) => {
  return tlRender(testComponent, options,
    () => {
      return {
        stubs: {
          'font-awesome-icon': true
        },
        i18n,
      }
    })
}
