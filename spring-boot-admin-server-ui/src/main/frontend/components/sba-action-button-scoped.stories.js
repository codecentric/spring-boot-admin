import SbaActionButtonScoped from './sba-action-button-scoped.vue';
import i18n from '@/i18n';

export default {
  component: SbaActionButtonScoped,
  title: 'SBA Components/Action Button Scoped',
};

const TemplateWithProps = (args, {argTypes}) => ({
  components: {SbaActionButtonScoped},
  props: Object.keys(argTypes),
  template: '<sba-action-button-scoped v-bind="$props" />',
  i18n
});

export const OneInstanceSuccessful = TemplateWithProps.bind({});
OneInstanceSuccessful.args = {
  instanceCount: 1,
  label: 'Label',
  actionFn() {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve()
      }, 2000);
    });
  },
};

export const MultipleInstancesSuccessful = TemplateWithProps.bind({});
MultipleInstancesSuccessful.args = {
  ...OneInstanceSuccessful.args,
  instanceCount: 10
};

export const OneInstanceFailing = TemplateWithProps.bind({});
OneInstanceFailing.args = {
  instanceCount: 1,
  label: 'Label',
  actionFn() {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        reject()
      }, 2000);
    });
  },
};

const TemplateWithSlot = (args, {argTypes}) => ({
  components: {SbaActionButtonScoped},
  props: Object.keys(argTypes),
  template: `
    <sba-action-button-scoped v-bind="$props">
    <template v-slot="slotProps">
      <span v-if="slotProps.refreshStatus === 'completed'">Status Is Completed</span>
      <span v-else-if="slotProps.refreshStatus === 'failed'">Status Is Failed</span>
      <span v-else>Default Label</span>
    </template>
    </sba-action-button-scoped>`,
  i18n
});

export const SlottedOneInstanceSuccessful = TemplateWithSlot.bind({});
SlottedOneInstanceSuccessful.args = {
  instanceCount: 1,
  actionFn() {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve()
      }, 2000);
    });
  },
};

export const SlottedOneInstanceFailing = TemplateWithSlot.bind({});
SlottedOneInstanceFailing.args = {
  instanceCount: 1,
  actionFn() {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        reject()
      }, 2000);
    });
  },
};
