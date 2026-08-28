import { setup } from '@storybook/vue3-vite';
import { mswLoader } from 'msw-storybook-addon/csf3';
import { createRouter, createWebHistory } from 'vue-router';

import './storybook.css';
import '@/index.css';

import components from '@/components';
import { createApplicationStore } from '@/composables/useApplicationStore';
import i18n from '@/i18n';
import applicationsEndpoint from '@/mocks/applications';
import mappingsEndpoint from '@/mocks/instance/mappings';

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

export const preview = {
  parameters,
  loaders: [mswLoader()],
  tags: ['autodocs'],
};
