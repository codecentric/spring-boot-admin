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
