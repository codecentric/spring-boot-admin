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

import i18n from '@/i18n';
import SbaPaginationNav from '@/components/sba-pagination-nav';

export default {
  component: SbaPaginationNav,
  title: 'SBA Components/Pagination',
};

const Template = (args, {argTypes}) => {
  return ({
    components: {SbaPaginationNav},
    props: Object.keys(argTypes),
    methods: {
      change($event) {
        this.current = $event;
      }
    },
    data() {
      return {
        current: 1
      }
    },
    template: `
      <sba-pagination-nav v-model="current" v-bind="$props" @change="change"/>
    `,
    i18n
  });
};

export const NoPages = Template.bind({});
NoPages.args = {
  pageCount: 0,
};

export const OnePage = Template.bind({});
OnePage.args = {
  pageCount: 1,
};

export const ManyPages = Template.bind({});
ManyPages.args = {
  pageCount: 12,
};
