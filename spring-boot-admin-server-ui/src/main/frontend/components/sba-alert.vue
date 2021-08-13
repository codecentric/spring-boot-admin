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
  <div class="message" :class="alertClass" role="alert" v-if="hasError">
    <div class="message-body">
      <strong>
        <font-awesome-icon :class="iconClass" icon="exclamation-triangle" />&nbsp;<span v-text="title" />
      </strong>
      <p v-text="error.message" />
    </div>
  </div>
</template>

<script>
export const Severity = {
  ERROR: 'ERROR',
  WARN: 'WARN',
  INFO: 'INFO',
  SUCCESS: 'SUCCESS',
};

export default {
  name: 'SbaAlert',
  props: {
    title: {
      type: String,
      required: true
    },
    error: {
      type: Error,
      default: null
    },
    severity: {
      type: String,
      default: 'ERROR'
    }
  },
  data() {
    return {
      alertClass: {
        'is-danger': this.severity.toUpperCase() === Severity.ERROR,
        'is-warning': this.severity.toUpperCase() === Severity.WARN,
        'is-info': this.severity.toUpperCase() === Severity.INFO,
        'is-success': this.severity.toUpperCase() === Severity.SUCCESS,
      },
      iconClass: {
        'has-text-danger': this.severity.toUpperCase() === Severity.ERROR,
        'has-text-warning': this.severity.toUpperCase() === Severity.WARN
      }
    }
  },
  methods: {
    hasError() {
      return this.error !== undefined && this.error !== null;
    }
  }
}
</script>
