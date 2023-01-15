<!--
  - Copyright 2014-2018 the original author or authors.
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
  <div class="flex items-center gap-1">
    <sba-input
      v-if="!hasComplexValue"
      ref="inputRef"
      v-model="input"
      :disabled="!editing"
      :error="error"
      :hint="descriptor.desc"
      :label="name"
      :name="name"
      :readonly="!editing"
      :title="input"
      class="flex-1"
      @dblclick="edit"
      @keyup.esc="cancel"
      @keyup.enter="save"
    >
      <template #prepend>
        <button :disabled="!descriptor.rw" class="button" @click="edit">
          <font-awesome-icon v-if="descriptor.rw" icon="pencil-alt" />
          <font-awesome-icon v-else icon="eye" />
        </button>
      </template>
    </sba-input>

    <sba-button-group v-if="editing">
      <sba-button @click="cancel">
        {{ $t('term.cancel') }}
      </sba-button>
      <sba-button
        :class="{ 'is-loading': saving }"
        :disabled="value === input"
        primary
        @click="save"
      >
        {{ $t('term.save') }}
      </sba-button>
    </sba-button-group>
  </div>
</template>

<script>
import { responseHandler } from '@/views/instances/jolokia/responseHandler';

export default {
  props: {
    name: {
      type: String,
      required: true,
    },
    descriptor: {
      type: Object,
      required: true,
    },
    value: {
      type: [Object, String, Number, null],
      required: true,
    },
    onSaveValue: {
      type: Function,
      required: true,
    },
  },
  data() {
    return {
      input: this.value,
      editing: false,
      saving: false,
      error: null,
    };
  },
  computed: {
    hasComplexValue() {
      return this.value !== null && typeof this.value === 'object';
    },
    jsonValue() {
      return JSON.stringify(this.value, null, 4);
    },
  },
  watch: {
    value(val) {
      this.input = val;
    },
  },
  methods: {
    async edit() {
      if (this.descriptor.rw && !this.hasComplexValue) {
        this.editing = true;
        await this.$nextTick();
        this.$refs.inputRef.focus?.();
      }
    },
    cancel() {
      this.editing = false;
    },
    async save() {
      this.saving = true;
      try {
        let response = await this.onSaveValue(this.input);
        const { result, error } = responseHandler(response);
        this.result = result;
        this.error = error;
        this.editing = false;
      } catch (error) {
        console.warn(`Error saving attribute ${this.name}`, error);
        this.error = error;
      } finally {
        this.saving = false;
      }
    },
  },
};
</script>
