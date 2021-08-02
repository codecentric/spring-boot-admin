import SbaTags from './sba-tags';

export default {
  component: SbaTags,
  title: 'SBA Components/Tags',
};

const Template = (args, {argTypes}) => ({
  components: {SbaTags},
  props: Object.keys(argTypes),
  template: '<sba-tags v-bind="$props" />'
});

export const Tags = Template.bind({});
Tags.args = {
  tags: {
    'This is a key': 'This a value',
    simpleKey: 'value',
  }
};
