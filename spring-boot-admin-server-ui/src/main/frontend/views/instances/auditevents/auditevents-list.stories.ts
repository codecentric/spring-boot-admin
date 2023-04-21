import { applications } from '../../../mocks/applications/data';
import Instance from '../../../services/instance';
import Index from './index.vue';

export default {
  component: Index,
  title: 'SBA View/AuditeventsList',
};

const Template = (args, { argTypes }) => ({
  components: { Index },
  props: Object.keys(argTypes),
  template: '<index v-bind="$props" />',
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
