import { app } from '@storybook/vue3';

import '@/index.css';

import components from '@/components';
import i18n from '@/i18n';

app.use(i18n);
app.use(components);

export const parameters = {
  actions: { argTypesRegex: '^on[A-Z].*' },
  controls: {
    matchers: {
      color: /(background|color)$/i,
      date: /Date$/,
    },
  },
};
