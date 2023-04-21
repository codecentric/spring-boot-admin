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
import Popper from 'popper.js';

const poppers = new WeakMap();

const mounted = (el, binding) => {
  const reference =
    typeof binding.value === 'string'
      ? document.getElementById(binding.value)
      : binding.value;
  if (reference) {
    const popper = new Popper(reference, el);
    poppers.set(el, popper);
  }
};

const beforeUnmount = (el) => {
  const popper = poppers.get(el);
  if (popper) {
    popper.destroy(el);
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
  beforeUnmount,
};
