import NotificationcenterPlugin from '@stekoe/vue-toast-notificationcenter';
import { render as tlRender } from '@testing-library/vue';
import { RouterLinkStub } from '@vue/test-utils';
import { merge } from 'lodash-es';
import { createI18n } from 'vue-i18n';
import { createRouter, createWebHashHistory } from 'vue-router';

import components from './components/index.js';
import SbaModalPlugin from './plugins/modal';

import { createViewRegistry } from '@/composables/ViewRegistry';
import ViewRegistry from '@/viewRegistry';

let terms = {};
const modules: Record<string, any> = import.meta.glob('@/**/i18n.en.json', {
  eager: true,
});
for (const modulesKey in modules) {
  terms = { ...terms, ...modules[modulesKey] };
}
export let router;
createViewRegistry();

export const render = (testComponent, options?) => {
  const routes = [{ path: '/', component: testComponent }];
  if (testComponent.install) {
    const viewRegistry = new ViewRegistry();
    testComponent.install({ viewRegistry });
    const routeForComponent = viewRegistry._toRoutes(() => true)[0];

    routes.push({
      ...routeForComponent,
      path: '/' + routeForComponent.path,
    });
  }

  router = createRouter({
    history: createWebHashHistory(),
    routes: routes,
  });

  const renderOptions = merge(
    {
      global: {
        plugins: [
          router,
          createI18n({
            locale: options?.locale || 'en',
            messages: {
              en: terms,
            },
            legacy: false,
            fallbackWarn: false,
            missingWarn: false,
          }),
          NotificationcenterPlugin,
          SbaModalPlugin,
          components,
        ],
        stubs: { 'font-awesome-icon': true, RouterLink: RouterLinkStub },
      },
    },
    options
  );
  const utils = tlRender(testComponent, renderOptions);
  return { ...utils };
};
