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


import SbaAlert from './sba-alert.vue';

export default {
  component: SbaAlert,
  title: 'SBA Components/Alert',
};

const Template = (args, {argTypes}) => ({
  components: {SbaAlert},
  props: Object.keys(argTypes),
  template: '<sba-alert v-bind="$props" />',
});

export const AlertError = Template.bind({});
AlertError.args = {
  title: 'Titel',
  error: new Error('Message'),
  severity: 'ERROR'
};

export const AlertWarning = Template.bind({});
AlertWarning.args = {
  ...AlertError.args,
  severity: 'WARN'
};

export const AlertInfo = Template.bind({});
AlertInfo.args = {
  ...AlertError.args,
  severity: 'INFO'
};

export const AlertSuccess = Template.bind({});
AlertSuccess.args = {
  ...AlertError.args,
  severity: 'SUCCESS'
};
