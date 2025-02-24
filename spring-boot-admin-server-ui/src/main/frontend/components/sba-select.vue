<!--
  - Copyright 2014-2020 the original author or authors.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <div>
    <div>
      <label
        v-if="hasLabel"
        :for="id"
        class="block text-sm font-medium text-gray-700"
        v-text="label"
      />
      <div class="flex rounded shadow-sm" :class="{ 'mt-1': hasLabel }">
        <span
          v-if="$slots.prepend"
          class="inline-flex items-center px-3 rounded-l border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm"
        >
          <slot name="prepend" />
        </span>

        <select
          :id="id"
          :name="name"
          :value="modelValue"
          :autocomplete="autocomplete"
          class="focus:z-10 p-2 relative flex-1 block w-full rounded-none sm:text-sm bg-opacity-40 backdrop-blur-sm"
          :class="classNames(inputFieldClassNames, inputClass)"
          @input="handleInput"
        >
          <option v-for="(option, idx) in options" :key="idx" v-bind="option">
            {{ option.label }}
          </option>
        </select>

        <span
          v-if="$slots.append"
          class="inline-flex items-center px-3 rounded-r border border-l-0 border-gray-300 bg-gray-50 text-gray-500 text-sm"
        >
          <slot name="append" />
        </span>
      </div>
      <span v-if="$slots.info" class="mt-2 text-sm text-gray-500">
        <slot name="info" />
      </span>
    </div>
    <div v-if="error || hint" class="py-2">
      <div v-if="hint && !error" class="text-xs text-gray-500" v-text="hint" />
      <div v-if="error" class="text-xs text-red-500" v-text="error" />
    </div>
  </div>
</template>

<script>
import classNames from 'classnames';

export default {
  props: {
    name: {
      type: String,
      required: true,
    },
    label: {
      type: String,
      default: null,
    },
    placeholder: {
      type: String,
      default: null,
    },
    type: {
      type: String,
      default: 'text',
    },
    modelValue: {
      type: [String, Number],
      default: null,
    },
    min: {
      type: Number,
      default: undefined,
    },
    list: {
      type: Array,
      default: undefined,
    },
    inputClass: {
      type: [String, Array, Object],
      default: undefined,
    },
    error: {
      type: String,
      default: undefined,
    },
    hint: {
      type: String,
      default: undefined,
    },
    autocomplete: {
      type: String,
      default: undefined,
    },
    options: {
      type: Array,
      required: true,
    },
  },
  emits: ['update:modelValue', 'input'],
  computed: {
    hasLabel() {
      return this.label !== null && this.label.trim() !== '';
    },
    id() {
      return (this.name || '').replace(/[^\w]/gi, '');
    },
    inputFieldClassNames() {
      const hasAppend = this.hasSlot('append');
      const hasPrepend = this.hasSlot('prepend');

      const classNames = [];

      if (this.error !== undefined) {
        classNames.push(
          'focus:ring-red-500 focus:border-red-500 border-red-400',
        );
      } else {
        classNames.push(
          'focus:ring-indigo-500 focus:border-indigo-500 border-gray-300',
        );
      }

      if (!hasAppend) {
        classNames.push('rounded-r');
      }
      if (!hasPrepend) {
        classNames.push('rounded-l');
      }

      return classNames;
    },
  },
  methods: {
    classNames,

    handleInput($event) {
      this.$emit('update:modelValue', $event.target.value);
      this.$emit('input', $event.target.value);
    },
    hasSlot(slot) {
      return !!this.$slots[slot] && Object.keys(this.$slots[slot]).length > 0;
    },
  },
};
</script>
