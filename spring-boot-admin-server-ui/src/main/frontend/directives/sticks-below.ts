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

const mounted = (el, binding) => {
  if (!binding.value) {
    return;
  }

  const targetElement = document.querySelector(binding.value);
  if (targetElement) {
    const clientRect = targetElement.getBoundingClientRect();
    const top = clientRect.height + clientRect.top;

    el.style.top = `${top}px`;
    el.style.position = 'sticky';
  }
};

export default {
  mounted,
  update(el, binding) {
    if (binding.value === binding.oldValue) {
      return;
    }
    mounted(el, binding);
  },
};
