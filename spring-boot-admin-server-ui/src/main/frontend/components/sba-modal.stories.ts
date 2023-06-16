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
import SbaModal from './sba-modal.vue';

import i18n from '@/i18n';

export default {
  component: SbaModal,
  title: 'Components/Modal',
};

const Template = (args) => ({
  components: { SbaModal },
  setup() {
    return {
      args,
    };
  },
  template: `
    <sba-modal v-bind="args">
      <template v-if="${'header' in args}" #header>${args.header}</template>
      <template v-if="${'body' in args}" #body>${args.body}</template>
      <template v-if="${'footer' in args}" #footer>${args.footer}</template>
    </sba-modal>
  `,
  i18n,
});

export const ModalWithBody = {
  render: Template,

  args: {
    modelValue: true,
    body: 'I am a body',
  },
};

export const ModalWithHeaderAndFooter = {
  render: Template,

  args: {
    modelValue: true,
    header: 'You are awesome!',
    body: '<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla accumsan, metus ultrices eleifend gravida, nulla nunc varius lectus, nec rutrum justo nibh eu lectus.</p>',
    footer: '<sba-button class="button">Close me!</sba-button>',
  },
};
