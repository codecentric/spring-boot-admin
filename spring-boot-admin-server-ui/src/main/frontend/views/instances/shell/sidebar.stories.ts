import { applications } from '../../../mocks/applications/data';
import Instance from '../../../services/instance';
import Sidebar from './sidebar.vue';

export default {
  component: Sidebar,
  title: 'Sidebar',
};

const Template = (args, { argTypes }) => ({
  components: { Sidebar },
  props: Object.keys(argTypes),
  template: '<Sidebar v-bind="$props" />',
});

export const Test = {
  render: Template,

  args: {
    instance: new Instance({
      id: 'bba333956ae6',
      ...applications[0].instances[0],
    }),
  },
};
