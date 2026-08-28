import { vueRouter } from 'storybook-vue3-router';
import { defineComponent, h, markRaw } from 'vue';

import { applications } from '../../../mocks/applications/data';
import Instance from '../../../services/instance';
import Sidebar from './sidebar.vue';

import i18n from '@/i18n';
import { VIEW_GROUP } from '@/views/ViewGroup';

const TestComponent = defineComponent({
  render() {
    return h('div');
  },
});

const views = [
  'webOverviewView',
  'webHealthView',
  'dataEnvView',
  'dataConfigpropsView',
].map((name, index) => ({
  id: name,
  name: `instances/${name
    .replace('View', '')
    .replace(/([A-Z])/g, '/$1')
    .toLowerCase()}`,
  parent: 'instances',
  handle: markRaw(TestComponent),
  order: index + 1,
  component: markRaw(TestComponent),
  group: index < 2 ? VIEW_GROUP.WEB : VIEW_GROUP.DATA,
  hasChildren: false,
  props: {},
  isEnabled: () => true,
  label: name,
}));

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
    views,
    application: applications[0],
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
