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
      <div :class="{ 'mt-1': hasLabel }" class="flex rounded shadow-sm">
        <!-- PREPEND -->
        <label
          v-if="$slots.prepend"
          :for="id"
          class="inline-flex items-center px-3 rounded-l border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm"
        >
          <slot name="prepend" />
        </label>

        <!-- INPUT -->
        <datalist :id="datalistId">
          <option
            v-for="optionName in list"
            :key="optionName"
            v-text="optionName"
          />
        </datalist>
        <input
          :id="id"
          :autocomplete="autocomplete"
          :class="classNames(inputFieldClassNames, inputClass)"
          :disabled="disabled"
          :list="datalistId"
          :min="min"
          :name="name"
          :placeholder="placeholder"
          :readonly="readonly"
          :type="type"
          :value="modelValue"
          :aria-label="label || placeholder"
          :autofocus="autofocus"
          class="focus:z-10 p-2 relative flex-1 block w-full rounded-none bg-opacity-40 backdrop-blur-sm"
          @input="handleInput"
        />
        <!-- APPEND -->
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
    disabled: {
      type: Boolean,
      default: false,
    },
    readonly: {
      type: Boolean,
      default: false,
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
    autofocus: {
      type: Boolean,
      default: undefined,
    },
  },
  emits: ['update:modelValue', 'input'],
  computed: {
    hasLabel() {
      return this.label !== null && this.label.trim() !== '';
    },
    id() {
      let number = 'input-' + Math.floor(Math.random() * Date.now());
      return (this.name || number).replace(/[^\w]/gi, '');
    },
    datalistId() {
      return 'listId-' + this._.uid;
    },
    inputFieldClassNames() {
      const hasAppend = this.hasSlot('append');
      const hasPrepend = this.hasSlot('prepend');

      const classNames = [];

      if (this.error !== null && this.error !== undefined) {
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

      if (this.disabled === true) {
        classNames.push('cursor-not-allowed');
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
