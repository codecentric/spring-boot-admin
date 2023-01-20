import NotificationcenterPlugin from '@stekoe/vue-toast-notificationcenter';
import { render as tlRender } from '@testing-library/vue';
import { RouterLinkStub } from '@vue/test-utils';
import { merge } from 'lodash-es';
import { createI18n } from 'vue-i18n';
import { createRouter, createWebHashHistory } from 'vue-router';

import components from './components/index.js';
import terms from './i18n/i18n.en.json';
import SbaModalPlugin from './plugins/modal';

import ViewRegistry from '@/viewRegistry';

export let router;

export const render = (testComponent, options) => {
  let routes = [{ path: '/', component: testComponent }];
  if (testComponent.install) {
    let viewRegistry = new ViewRegistry();
    testComponent.install({ viewRegistry });
    let routeForComponent = viewRegistry._toRoutes(
      viewRegistry._views,
      () => true
    )[0];

    routes.push({
      ...routeForComponent,
      path: '/' + routeForComponent.path,
    });
  }
  router = createRouter({
    history: createWebHashHistory(),
    routes: routes,
  });

  let renderOptions = merge(
    {
      global: {
        plugins: [
          router,
          createI18n({
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
  return tlRender(testComponent, renderOptions);
};
