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
const install = (app) => {
  app.use(components);
  app.use(i18n);
  app.use(router);
  app.use(applicationStore);
};

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
  loader: { '.js': 'jsx' },
};

export const preview = {
  parameters,
  loaders: [mswLoader()],
  tags: ['autodocs'],
  decorators: [
    (story) => ({
      components: { story },
      setup() {
        return { install };
      },
      template: '<story />',
      mounted() {
        install(this.$.appContext.app);
      },
    }),
  ],
};
