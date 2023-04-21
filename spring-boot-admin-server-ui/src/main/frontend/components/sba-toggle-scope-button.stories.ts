import { reactive } from 'vue';

import { ActionScope } from './ActionScope.js';
import SbaToggleScopeButton from './sba-toggle-scope-button.vue';

export default {
  component: SbaToggleScopeButton,
  title: 'Components/Buttons/Toggle Scope Button',
};

const Template = (args) => ({
  components: { SbaToggleScopeButton },
  setup() {
    return reactive({ args });
  },
  template:
    '<sba-toggle-scope-button v-bind="args" v-model:model-value="args.modelValue" />',
});

export const Default = {
  render: Template,

  args: {
    modelValue: ActionScope.INSTANCE,
    instanceCount: 2,
    showInfo: true,
  },
};
