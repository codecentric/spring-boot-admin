import SbaTag from './sba-tag.vue';

export default {
  component: SbaTag,
  title: 'Components/Tag',
  argTypes: {
    value: {
      description: 'A value to show',
      example: 'asd',
      control: 'text',
    },
    label: {
      description: 'An optional label',
      example: 'asd',
      control: 'text',
    },
    small: {
      description: 'A single value to show',
      control: 'boolean',
    },
  },
};

const Template = (args) => <sba-tag {...args} />;

export const Tag = {
  render: Template,

  args: {
    value: 'I am a tag',
  },
};
