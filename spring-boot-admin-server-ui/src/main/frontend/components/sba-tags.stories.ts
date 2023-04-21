import SbaTags from './sba-tags.vue';

export default {
  component: SbaTags,
  title: 'Components/Tags',
};

const Template = (args) => ({
  components: { SbaTags },
  setup() {
    return { args };
  },
  template: '<SbaTags v-bind="args" />',
});

export const SingleTag = {
  render: Template,

  args: {
    tags: {
      'This is a key': 'This a value',
    },
  },
};

export const SingleTagSmall = {
  render: Template,

  args: {
    small: true,
    tags: {
      'This is a key': 'This a value',
    },
  },
};

export const MultipleTags = {
  render: Template,

  args: {
    tags: {
      'This is a key': 'This a value',
      simpleKey: 'value',
    },
  },
};
