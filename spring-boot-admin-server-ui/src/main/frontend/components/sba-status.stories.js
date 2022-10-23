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

export const Status = Template.bind({});

export const StatusUp = Template.bind({});
StatusUp.args = {
  date: Date.now(),
  status: 'UP',
};

export const StatusRestricted = Template.bind({});
StatusRestricted.args = {
  ...StatusUp.args,
  status: 'RESTRICTED',
};

export const StatusOos = Template.bind({});
StatusOos.args = {
  ...StatusUp.args,
  status: 'OUT_OF_SERVICE',
};

export const StatusDown = Template.bind({});
StatusDown.args = {
  ...StatusUp.args,
  status: 'DOWN',
};

export const StatusOffline = Template.bind({});
StatusOffline.args = {
  ...StatusUp.args,
  status: 'OFFLINE',
};

export const StatusUnknown = Template.bind({});
StatusUnknown.args = {
  ...StatusUp.args,
  status: 'UNKNOWN',
};
