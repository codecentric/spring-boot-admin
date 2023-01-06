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
    <div class="control">
      <button
        class="button is-light is-fullwidth columns has-text-left"
        @click="$emit('click', $event)"
      >
        <small
          class="is-light is-muted column is-flex-grow-0 is-flex-shrink-0 p-1"
          :title="descriptor.ret"
          v-text="shortenedRet"
        />
        <span
          class="column is-flex-grow-1 is-flex-shrink-0 p-1 is-truncated"
          :title="name"
          v-text="shortenedName"
        />
      </button>
      <p class="help" v-text="descriptor.desc" />
    </div>
  </div>
</template>

<script>
import { truncateJavaType } from '@/views/instances/jolokia/utils';

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
  },
  emits: ['click'],
  computed: {
    shortenedName() {
      return truncateJavaType(this.name);
    },
    shortenedRet() {
      return truncateJavaType(this.descriptor.ret);
    },
  },
};
</script>

<style>
.is-truncated {
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
