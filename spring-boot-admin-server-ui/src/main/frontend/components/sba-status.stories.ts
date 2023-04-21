import SbaStatus from './sba-status.vue';

export default {
  component: SbaStatus,
  title: 'Components/Status',
};

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

export const StatusRestricted = {
  render: Template,

  args: {
    ...StatusUp.args,
    status: 'RESTRICTED',
  },
};

export const StatusOos = {
  render: Template,

  args: {
    ...StatusUp.args,
    status: 'OUT_OF_SERVICE',
  },
};

export const StatusDown = {
  render: Template,

  args: {
    ...StatusUp.args,
    status: 'DOWN',
  },
};

export const StatusOffline = {
  render: Template,

  args: {
    ...StatusUp.args,
    status: 'OFFLINE',
  },
};

export const StatusUnknown = {
  render: Template,

  args: {
    ...StatusUp.args,
    status: 'UNKNOWN',
  },
};
