import { setup } from '@storybook/vue3';
import { initialize, mswDecorator } from 'msw-storybook-addon';
import { createRouter, createWebHistory } from 'vue-router';

import './storybook.css';
import '@/index.css';

import components from '@/components';
import { createApplicationStore } from '@/composables/useApplicationStore';
import i18n from '@/i18n';
import applicationsEndpoint from '@/mocks/applications';
import mappingsEndpoint from '@/mocks/instance/mappings';

initialize();
const router = createRouter({
  history: createWebHistory(),
  routes: [],
});

const applicationStore = createApplicationStore();
setup((app) => {
  app.use(components);
  app.use(i18n);
  app.use(router);
  app.use(applicationStore);
});

export const parameters = {
  actions: { argTypesRegex: '^on[A-Z].*' },
  controls: {
    matchers: {
      color: /(background|color)$/i,
      date: /Date$/,
    },
  },
  msw: {
    handlers: {
      auth: null,
      others: [...mappingsEndpoint, ...applicationsEndpoint],
    },
  },
};

export const decorators = [mswDecorator];
