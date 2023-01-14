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
  <button
    :title="$t('instances.jolokia.execute')"
    class="text-left mb-3 block flex items-center"
    @click="$emit('click', $event)"
  >
    <font-awesome-icon class="mr-2 hidden md:block" icon="cogs" />
    <div>
      <span :title="name" v-text="shortenedName" />:
      <small
        :title="descriptor.ret"
        class="text-gray-400"
        v-text="shortenedRet"
      />
      <p
        v-if="showDescription"
        class="text-gray-400 text-xs mt-1"
        v-text="descriptor.desc"
      />
    </div>
  </button>
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
    showDescription() {
      let name = this.name.split('(').shift();
      return name !== this.descriptor.desc;
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