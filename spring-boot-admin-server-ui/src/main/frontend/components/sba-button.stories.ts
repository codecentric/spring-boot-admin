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
import SbaButton from './sba-button.vue';

export default {
  component: SbaButton,
  title: 'Components/Buttons/Button',
};

const Template = (args) => {
  return {
    components: { SbaButton },
    setup() {
      return { args };
    },
    template: `<sba-button v-bind="args">${args.label}</sba-button>`,
  };
};

export const DefaultButton = {
  render: Template,

  args: {
    label: 'Default button',
  },
};

export const PrimaryButton = {
  render: Template,

  args: {
    label: 'Primary button',
    primary: 'primary',
  },
};

export const DangerButton = {
  render: Template,

  args: {
    label: 'Danger button',
    class: 'is-danger',
  },
};

export const SuccessButton = {
  render: Template,

  args: {
    label: 'Danger button',
    class: 'is-success',
  },
};

const SizeTemplate = () => {
  return {
    components: { SbaButton },
    template: `
      <sba-button size="xs">button xs</sba-button>
      <sba-button size="sm">button sm</sba-button>
      <sba-button size="base">button base</sba-button>
    `,
  };
};

export const ButtonSizes = {
  render: SizeTemplate,
};
