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
import SbaConfirmButton from './sba-confirm-button.vue';

export default {
  component: SbaConfirmButton,
  title: 'Components/Buttons/Confirm Button',
};

const Template = (args) => {
  return {
    components: { SbaConfirmButton },
    setup() {
      return { args };
    },
    template: `<sba-confirm-button v-bind="args">${args.label}</sba-confirm-button>`,
  };
};

export const DefaultButton = {
  render: Template,

  args: {
    label: 'Default confirm button',
  },
};
