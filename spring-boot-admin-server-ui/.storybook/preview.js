import "../src/main/frontend/index.css";
import "./storybook.css";

import {app} from '@storybook/vue3';
import i18n from "../src/main/frontend/i18n/index.js";
import mappingsEndpoint from '../src/main/frontend/mocks/instance/mappings';
import applicationsEndpoint from "../src/main/frontend/mocks/applications";

import {initialize, mswDecorator} from 'msw-storybook-addon';
import components from "../src/main/frontend/components/index.js";

initialize();

app.use(components);
app.use(i18n);

export const parameters = {
  actions: {argTypesRegex: "^on[A-Z].*"},
  controls: {
    matchers: {
      color: /(background|color)$/i,
      date: /Date$/,
    },
  },
  msw: {
    handlers: {
      auth: null,
      others: [...mappingsEndpoint, ...applicationsEndpoint]
    }
  }
}

export const decorators = [
  mswDecorator,
]

