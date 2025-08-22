import { vueRouter } from 'storybook-vue3-router';

import { applications } from '../../../mocks/applications/data';
import Instance from '../../../services/instance';
import Sidebar from './sidebar.vue';

import i18n from '@/i18n';

export default {
  component: Sidebar,
  title: 'Sidebar',
};

const TemplateWithProps = (args) => ({
  components: { Sidebar },
  setup() {
    return { args };
  },
  template: '<Sidebar v-bind="args" />',
  i18n,
});

export const Test = {
  render: TemplateWithProps,

  args: {
    instance: new Instance({
      id: 'bba333956ae6',
      ...applications[0].instances[0],
    }),
  },
  decorators: [
    vueRouter(
      [
        {
          name: 'instances/details',
          path: '/',
          component: TemplateWithProps,
        },
      ],
      { initialRoute: '/' },
    ),
  ],
};
