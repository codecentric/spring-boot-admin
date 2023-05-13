import { setup } from '@storybook/vue3';
import { initialize, mswDecorator } from 'msw-storybook-addon';

import './storybook.css';
import '@/index.css';

import components from '@/components';

import i18n from '@/i18n';
import applicationsEndpoint from '@/mocks/applications';
import mappingsEndpoint from '@/mocks/instance/mappings';

initialize();

setup((app) => {
  app.use(components);
  app.use(i18n);
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
