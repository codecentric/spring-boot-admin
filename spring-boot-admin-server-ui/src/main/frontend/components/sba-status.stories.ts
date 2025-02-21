import { Meta, StoryObj } from '@storybook/vue3';

import SbaStatus from './sba-status.vue';

const meta: Meta<typeof SbaStatus> = {
  component: SbaStatus,
  title: 'Components/Status',
};
export default meta;

type Story = StoryObj<typeof SbaStatus>;

const Template = (args) => ({
  components: { SbaStatus },
  setup() {
    return { args };
  },
  template: '<sba-status v-bind="args" />',
});

export const Status = {
  render: Template,
};

export const StatusUp = {
  render: Template,

  args: {
    date: Date.now(),
    status: 'UP',
  },
};

export const StatusRestricted: Story = {
  render: Template,

  args: {
    ...StatusUp.args,
    status: 'RESTRICTED',
  },
};

export const StatusOos: Story = {
  render: Template,

  args: {
    ...StatusUp.args,
    status: 'OUT_OF_SERVICE',
  },
};

export const StatusDown: Story = {
  render: Template,

  args: {
    ...StatusUp.args,
    status: 'DOWN',
  },
};

export const StatusOffline: Story = {
  render: Template,

  args: {
    ...StatusUp.args,
    status: 'OFFLINE',
  },
};

export const StatusUnknown: Story = {
  render: Template,

  args: {
    ...StatusUp.args,
    status: 'UNKNOWN',
  },
};
