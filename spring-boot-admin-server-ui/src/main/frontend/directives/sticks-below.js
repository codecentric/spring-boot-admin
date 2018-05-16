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

const bind = (el, binding) => {
  if (!binding.value) {
    return;
  }
  const top = binding.value.map(v => document.querySelector(v))
    .filter(v => Boolean(v))
    .map(v => v.getBoundingClientRect().height)
    .reduce((a, b) => a + b, 0);
  el.style.top = `${top}px`;
  el.style.position = 'sticky';
};

export default {
  bind,
  update(el, binding) {
    if (binding.value === binding.oldValue) {
      return
    }
    bind(el, binding)
  }
}

