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
import SbaPaginationNav from './sba-pagination-nav.vue';

import i18n from '@/i18n';

export default {
  component: SbaPaginationNav,
  title: 'Components/Pagination',
};

const Template = (args) => {
  return {
    components: { SbaPaginationNav },
    setup() {
      return { args };
    },
    methods: {
      change($event) {
        this.current = $event;
      },
    },
    data() {
      return {
        current: 1,
      };
    },
    template: `
      <sba-pagination-nav v-bind="args" @update="change"/>
    `,
    i18n,
  };
};

export const NoPages = {
  render: Template,

  args: {
    pageCount: 0,
    modelValue: 1,
  },
};

export const OnePage = {
  render: Template,

  args: {
    pageCount: 1,
  },
};

export const ManyPages = {
  render: Template,

  args: {
    modelValue: 1,
    pageCount: 12,
  },
};
