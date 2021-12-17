import {render as tlRender} from '@testing-library/vue';
import VueI18n from 'vue-i18n';
import components from '@/components';
import Vue from 'vue';
import i18n from '@/i18n';
import VueRouter from 'vue-router';

Vue.use(VueI18n);
Vue.use(components);
Vue.use(VueRouter);

export const localVue = Vue;

export const render = (testComponent, options) => {
  return tlRender(testComponent, options,
    () => {
      return {
        stubs: {
          'font-awesome-icon': true
        },
        i18n,
        router: new VueRouter({
          mode: 'history',
          linkActiveClass: 'is-active',
        }),
      }
    })
}
