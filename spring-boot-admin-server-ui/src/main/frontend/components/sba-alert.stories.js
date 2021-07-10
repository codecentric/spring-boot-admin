import SbaAlert from './sba-alert.vue';

export default {
  component: SbaAlert,
  title: 'SBA Components/Alert',
};

const Template = (args, { argTypes }) => ({
  components: { SbaAlert },
  props: Object.keys(argTypes),
  template: '<sba-alert v-bind="$props" />',
});

export const AlertError = Template.bind({});
AlertError.args = {
  title: 'Titel',
  error: new Error('Message'),
  severity: 'ERROR'
};

export const AlertWarning = Template.bind({});
AlertWarning.args = {
  ...AlertError.args,
  severity: 'WARN'
};

export const AlertInfo = Template.bind({});
AlertInfo.args = {
  ...AlertError.args,
  severity: 'INFO'
};

export const AlertSuccess = Template.bind({});
AlertSuccess.args = {
  ...AlertError.args,
  severity: 'SUCCESS'
};
