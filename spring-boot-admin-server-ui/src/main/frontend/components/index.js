/*
 * Copyright 2014-2018 the original author or authors.
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


const components = [];

// eslint-disable-next-line no-undef
const context = require.context('.', false, /^(?:(?!.*\.spec\.(js|vue)$).)*\.(js|vue)$/);
context.keys().forEach(function (key) {
  const name = /^(.\/)+(.*)\.(vue|js)$/.exec(key)[2];
  components.push({name, component: context(key).default})
});

export default {
  install(Vue) {
    components.forEach(component => Vue.component(component.name, component.component));
  }
}