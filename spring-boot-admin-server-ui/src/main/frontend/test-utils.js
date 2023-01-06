import NotificationcenterPlugin from '@stekoe/vue-toast-notificationcenter';
import { render as tlRender } from '@testing-library/vue';
import { RouterLinkStub } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';

import components from './components/index.js';
import terms from './i18n/i18n.en.json';
import SbaModalPlugin from './plugins/modal';

export const render = (testComponent, options) => {
  return tlRender(testComponent, {
    ...options,
    global: {
      plugins: [
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
  });
};
