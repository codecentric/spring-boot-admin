import {render as tlRender} from '@testing-library/vue';
import VueI18n from 'vue-i18n';
import components from '@/components';

export const render = (testComponent, options) => {
  tlRender(testComponent, options,
    vue => {
      vue.use(VueI18n);
      vue.use(components);

      return {
        i18n: new VueI18n({
          missing: (locale, path) => path
        }),
      }
    })
}
