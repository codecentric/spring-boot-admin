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
import SbaKeyValueTable from './sba-key-value-table.vue';

export default {
  component: SbaKeyValueTable,
  title: 'Components/Key-Value Table',
};

const Template = (args) => {
  return {
    components: { SbaKeyValueTable },
    setup() {
      return { args };
    },
    template: `<sba-key-value-table v-bind="args">
      <template #customContent="slotProps">
        {{slotProps}}
      </template>
      <template #customId="slotProps">
        {{slotProps}}
      </template>
    </sba-key-value-table>`,
  };
};

export const Default = {
  render: Template,

  args: {
    map: {
      key1: 'value 1',
      key2: 'value 2',
      key3: 'value 3',
    },
  },
};

export const UsingSlots = {
  render: Template,

  args: {
    map: {
      ...Default.args.map,
      slotProps: 'Special Value',
      'a key with sapces': {
        id: 'customId',
        value: 'Custom value',
      },
    },
  },
};
