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
  <div class="field">
    <label class="label">
      <span v-text="name" />
      <small class="is-muted has-text-weight-normal" v-text="descriptor.type" />
    </label>
    <div class="field-body">
      <div class="field is-expanded">
        <div class="field has-addons">
          <div class="control">
            <button class="button" :disabled="!descriptor.rw" @click="edit">
              <font-awesome-icon icon="pencil-alt" />
            </button>
          </div>
          <div class="control is-expanded has-icons-right">
            <template v-if="!editing">
              <input v-if="!hasComplexValue" class="input" type="text" readonly :value="value" @dblclick="edit">
              <textarea v-else class="input m-bean-attribute--text" readonly v-text="jsonValue" />
              <span class="icon is-right has-text-warning" v-if="error">
                <font-awesome-icon icon="exclamation-triangle" />
              </span>
            </template>
            <template v-else>
              <input v-if="!hasComplexValue" class="input" type="text" ref="input"
                     v-model="input" @keyup.esc="cancel" @keyup.enter="save"
              >
            </template>
          </div>
        </div>
        <div class="help" v-text="descriptor.desc" />
      </div>
    </div>
    <div class="control" v-if="editing">
      <button class="button is-light is-small" @click="cancel">Cancel</button>
      <button class="button is-primary is-small" :class="{'is-loading' : saving}" @click="save"
              :disabled="value === input"
      >Save
      </button>
    </div>
  </div>
</template>

<script>
  export default {
    props: {
      name: {
        type: String,
        required: true
      },
      descriptor: {
        type: Object,
        required: true
      },
      value: {
        // eslint-disable-next-line vue/require-prop-type-constructor
        type: null,
        default: null
      },
      onSaveValue: {
        type: Function,
        required: true
      }
    },
    data: () => ({
      input: null,
      editing: false,
      saving: false,
      error: null
    }),
    computed: {
      hasComplexValue() {
        return this.value !== null && typeof this.value === 'object';
      },
      jsonValue() {
        return JSON.stringify(this.value, null, 4);
      },
    },
    methods: {
      async edit() {
        if (this.descriptor.rw && !this.hasComplexValue) {
          this.input = this.value;
          this.editing = true;
          await this.$nextTick();
          this.$refs.input.focus();
        }
      },
      cancel() {
        this.editing = false;
      },
      async save() {
        this.saving = true;
        try {
          await this.onSaveValue(this.input);
        } catch (error) {
          console.warn(`Error saving attribute ${this.name}`, error);
          this.error = error;
        } finally {
          this.saving = false;
          this.editing = false;
        }
      }
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .m-bean-attribute--text {
    resize: vertical;
    min-height: 120px;
  }
</style>
