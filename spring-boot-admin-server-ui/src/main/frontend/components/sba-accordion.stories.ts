/*
 * Copyright 2014-2025 the original author or authors.
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
import { ref } from 'vue';

import SbaAccordion from './sba-accordion.vue';

export default {
  component: SbaAccordion,
  title: 'Components/Accordion',
};

const Template = (args) => {
  return {
    components: { SbaAccordion },
    setup() {
      const isOpen = ref(args.modelValue ?? true);
      return { args, isOpen };
    },
    template: `
      <sba-accordion v-bind="args" v-model="isOpen">
        <template #title>
          ${args.title || 'Accordion Title'}
        </template>
        <template #actions v-if="${'actions' in args}">
          ${args.actions}
        </template>
        <template #default>
          ${args.slot}
        </template>
      </sba-accordion>
      <div class="mt-4 text-sm text-gray-600">
        Current state: <strong>{{ isOpen ? 'Open' : 'Closed' }}</strong>
      </div>
    `,
  };
};

export const DefaultOpen = {
  render: Template,

  args: {
    modelValue: true,
    title: 'Application Information',
    slot: `<article class="prose max-w-none">
      <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus vitae dolor ac ante ornare pharetra.
      Proin laoreet ex et lacinia hendrerit. Fusce sed justo at nulla pellentesque maximus sed at diam.</p>
      <p>Suspendisse sem lorem, lobortis vel orci quis, efficitur porta massa. In vel neque justo.
      Maecenas dapibus quam ut nisl porta, molestie egestas felis maximus.</p>
    </article>`,
  },
};

export const DefaultClosed = {
  render: Template,

  args: {
    modelValue: false,
    title: 'System Properties',
    slot: `<article class="prose max-w-none">
      <p>This content is initially hidden. Click the title to expand the accordion.</p>
    </article>`,
  },
};

export const WithKeyValueTable = {
  render: Template,

  args: {
    modelValue: true,
    title: 'Health Details',
    slot: `
      <div class="-mx-4 -my-3">
        <div class="bg-white px-4 py-3 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
          <dt class="text-sm font-medium text-gray-500">Status</dt>
          <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">UP</dd>
        </div>
        <div class="bg-gray-50 px-4 py-3 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
          <dt class="text-sm font-medium text-gray-500">Disk Space</dt>
          <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">10.5 GB free</dd>
        </div>
        <div class="bg-white px-4 py-3 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
          <dt class="text-sm font-medium text-gray-500">Database</dt>
          <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">Connected</dd>
        </div>
        <div class="bg-gray-50 px-4 py-3 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
          <dt class="text-sm font-medium text-gray-500">Memory</dt>
          <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">512 MB / 2 GB</dd>
        </div>
      </div>`,
  },
};

export const WithActions = {
  render: Template,

  args: {
    modelValue: true,
    title: 'Configuration Settings',
    actions:
      '<button class="text-sm text-blue-600 hover:text-blue-800">Edit</button>',
    slot: `<article class="prose max-w-none">
      <p>Configuration content with action buttons in the header.</p>
    </article>`,
  },
};

export const WithId = {
  render: (args) => {
    return {
      components: { SbaAccordion },
      setup() {
        const isOpen = ref(args.modelValue ?? true);
        return { args, isOpen };
      },
      template: `
        <div>
          <p class="mb-4 text-sm text-gray-600">
            This accordion has an ID and will persist its state in localStorage.
            Try toggling it and refreshing the page.
          </p>
          <sba-accordion v-bind="args" v-model="isOpen">
            <template #title>
              ${args.title || 'Persisted Accordion'}
            </template>
            <template #default>
              ${args.slot}
            </template>
          </sba-accordion>
          <div class="mt-4 text-sm text-gray-600">
            Current state: <strong>{{ isOpen ? 'Open' : 'Closed' }}</strong>
            <br />
            LocalStorage key: <code class="text-xs bg-gray-100 px-1 py-0.5 rounded">de.codecentric.spring-boot-admin.accordion.${args.id}.open</code>
          </div>
        </div>
      `,
    };
  },

  args: {
    id: 'storybook-example',
    modelValue: true,
    title: 'Persisted State Example',
    slot: `<article class="prose max-w-none">
      <p>This accordion's open/closed state is stored in localStorage using the ID "storybook-example".</p>
      <p>Try toggling it and refreshing the browser to see the state persist.</p>
    </article>`,
  },
};

export const MultipleAccordions = {
  render: (args) => {
    return {
      components: { SbaAccordion },
      setup() {
        const accordion1Open = ref(true);
        const accordion2Open = ref(false);
        const accordion3Open = ref(true);
        return { args, accordion1Open, accordion2Open, accordion3Open };
      },
      template: `
        <div class="space-y-4">
          <sba-accordion v-model="accordion1Open">
            <template #title>First Accordion</template>
            <template #default>
              <p>Content of the first accordion.</p>
            </template>
          </sba-accordion>

          <sba-accordion v-model="accordion2Open">
            <template #title>Second Accordion</template>
            <template #default>
              <p>Content of the second accordion (initially closed).</p>
            </template>
          </sba-accordion>

          <sba-accordion v-model="accordion3Open">
            <template #title>Third Accordion</template>
            <template #actions>
              <span class="text-xs text-gray-500">With Actions</span>
            </template>
            <template #default>
              <p>Content of the third accordion with actions.</p>
            </template>
          </sba-accordion>

          <div class="mt-4 text-sm text-gray-600">
            States:
            <strong>1: {{ accordion1Open ? 'Open' : 'Closed' }}</strong> |
            <strong>2: {{ accordion2Open ? 'Open' : 'Closed' }}</strong> |
            <strong>3: {{ accordion3Open ? 'Open' : 'Closed' }}</strong>
          </div>
        </div>
      `,
    };
  },

  args: {},
};

export const NestedContent = {
  render: Template,

  args: {
    modelValue: true,
    title: 'Advanced Configuration',
    slot: `
      <div class="space-y-4">
        <div>
          <h4 class="text-sm font-semibold mb-2">Server Settings</h4>
          <ul class="list-disc list-inside text-sm space-y-1">
            <li>Port: 8080</li>
            <li>Context Path: /admin</li>
            <li>SSL Enabled: false</li>
          </ul>
        </div>
        <div>
          <h4 class="text-sm font-semibold mb-2">Monitoring</h4>
          <ul class="list-disc list-inside text-sm space-y-1">
            <li>Interval: 10000ms</li>
            <li>Timeout: 5000ms</li>
            <li>Retries: 3</li>
          </ul>
        </div>
      </div>`,
  },
};
