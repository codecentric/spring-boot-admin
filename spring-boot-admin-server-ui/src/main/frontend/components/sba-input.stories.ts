/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import SbaInput from './sba-input.vue';

export default {
  component: SbaInput,
  title: 'Components/Form/Input',
};

const Template = (args) => {
  return {
    components: { SbaInput },
    setup() {
      return { args };
    },
    template: `
      <sba-input v-bind="args">${args.slots}</sba-input>
    `,
  };
};

export const SimpleInput = {
  render: Template,

  args: {
    modelValue: 'Hello there!',
  },
};

export const InputWithError = {
  render: Template,

  args: {
    ...SimpleInput.args,
    error: 'Please provide a value.',
  },
};

export const InputWithHint = {
  render: Template,

  args: {
    ...SimpleInput.args,
    hint: 'This is a hint',
  },
};

export const InputWithPrepend = {
  render: Template,

  args: {
    ...SimpleInput.args,
    slots: `
      <template #prepend>Prepend</template>
    `,
  },
};

export const InputWithAppend = {
  render: Template,

  args: {
    ...SimpleInput.args,
    inputClass: 'text-right',
    slots: `
      <template #append>EUR</template>
    `,
  },
};

export const Complex = {
  render: Template,

  args: {
    ...SimpleInput.args,
    label: 'Label',
    error: 'Please provide a value.',
    hint: 'I am a hint!',
  },
};
