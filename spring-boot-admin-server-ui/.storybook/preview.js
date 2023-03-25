import { app } from '@storybook/vue3';

import '@/index.css';

import i18n from '@/i18n/index.js';


app.use(i18n);

export const parameters = {
  actions: { argTypesRegex: '^on[A-Z].*' },
  controls: {
    matchers: {
      color: /(background|color)$/i,
      date: /Date$/,
    },
  },
};
