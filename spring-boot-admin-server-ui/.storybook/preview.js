import { withThemeByDataAttribute } from '@storybook/addon-styling';
import { app } from '@storybook/vue3';
import { createRouter, createWebHistory } from 'vue-router';

import './storybook.css';
import '@/index.css';

import components from '@/components';
import i18n from '@/i18n';

app.use(i18n);
app.use(components);
app.use(
  createRouter({
    history: createWebHistory(),
    linkActiveClass: 'is-active',
    routes: [],
  })
);

export const parameters = {
  actions: { argTypesRegex: '^on[A-Z].*' },
  layout: 'fullscreen',
  controls: {
    matchers: {
      color: /(background|color)$/i,
      date: /Date$/,
    },
  },
};

export const decorators = [
  withThemeByDataAttribute({
    themes: {
      light: 'light',
      dark: 'dark',
    },
    defaultTheme: 'light',
    attributeName: 'data-mode',
  }),
];
