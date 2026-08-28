import { vueRouter } from 'storybook-vue3-router';
import { defineComponent, h, markRaw } from 'vue';

import { applications } from '../../../mocks/applications/data';
import Instance from '../../../services/instance';
import Sidebar from './sidebar.vue';

import { VIEW_GROUP } from '@/views/ViewGroup';

const RouteComponent = markRaw(
  defineComponent({
    render() {
      return h('div');
    },
  }),
);

const createHandle = (text: string) =>
  markRaw(
    defineComponent({
      render() {
        return h('span', text);
      },
    }),
  );

const views = [
  { name: 'instances/web/overview', group: VIEW_GROUP.WEB, label: 'Overview' },
  { name: 'instances/web/health', group: VIEW_GROUP.WEB, label: 'Health' },
  { name: 'instances/data/env', group: VIEW_GROUP.DATA, label: 'Environment' },
  {
    name: 'instances/data/configprops',
    group: VIEW_GROUP.DATA,
    label: 'Config Props',
  },
].map((view, index) => ({
  id: view.name,
  name: view.name,
  parent: 'instances',
  handle: createHandle(view.label),
  order: (index + 1) * 10,
  component: RouteComponent,
  group: view.group,
  hasChildren: false,
  props: {},
  isEnabled: () => true,
  label: view.label,
}));

export default {
  component: Sidebar,
  title: 'Sidebar',
};

export const Test = {
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
        { name: 'instances/details', path: '/', component: RouteComponent },
        ...views.map((view) => ({
          name: view.name,
          path: `/${view.name}`,
          component: RouteComponent,
        })),
      ],
      { initialRoute: '/instances/web/health' },
    ),
  ],
};
