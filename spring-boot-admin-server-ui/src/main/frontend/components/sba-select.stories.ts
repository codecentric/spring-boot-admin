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
import SbaSelect from './sba-select.vue';

export default {
  component: SbaSelect,
  title: 'Components/Form/Select',
};

const Template = (args) => {
  return {
    components: { SbaSelect },
    setup() {
      return { args };
    },
    template: `
      <sba-select v-bind="args">
        <template #prepend>Prepend</template>
        <template #append>Append</template>
      </sba-select>
    `,
  };
};

export const SimpleSelect = {
  render: Template,

  args: {
    modelValue: 'berlin',
    options: [
      { value: 'beer', label: 'Beer' },
      { value: 'water', label: 'Water' },
      { value: 'wine', label: 'Wine' },
    ],
  },
};
