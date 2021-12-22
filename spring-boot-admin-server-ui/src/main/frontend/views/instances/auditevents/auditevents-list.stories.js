import Index from './index';
import Instance from "@/services/instance";
import {applications} from '@/mocks/applications/data'

export default {
  component: Index,
  title: 'SBA View/AuditeventsList',
};

const Template = (args, {argTypes}) => ({
  components: {Index},
  props: Object.keys(argTypes),
  template: '<index v-bind="$props" />'
});

export const Test = Template.bind({});
Test.args = {
  instance: new Instance({id: 'bba333956ae6', ...applications[0].instances[0]})
};

