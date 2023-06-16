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
import SbaButtonGroup from './sba-button-group.vue';
import SbaButton from './sba-button.vue';

export default {
  component: SbaButtonGroup,
  title: 'Components/Buttons/Button Group',
};

const Template = (args) => {
  return {
    components: { SbaButtonGroup, SbaButton },
    setup() {
      return { args };
    },
    template: `<sba-button-group v-bind="args">${args.slot}</sba-button-group>`,
  };
};

export const OneButton = {
  render: Template,

  args: {
    slot: '<sba-button>First</sba-button>',
  },
};

export const TwoButtons = {
  render: Template,

  args: {
    slot: `
      <sba-button>First</sba-button>
      <sba-button>Second</sba-button>
    `,
  },
};

export const MoreButtons = {
  render: Template,

  args: {
    slot: `
      <sba-button>First</sba-button>
      <sba-button>Second</sba-button>
      <sba-button>Third</sba-button>
    `,
  },
};
