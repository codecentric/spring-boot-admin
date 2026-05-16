<!--
  - Copyright 2014-2019 the original author or authors.
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
  <sba-button
    class="border-none sm:m-1 bg-transparent!"
    :title="title"
    size="xs"
    v-bind="$attrs"
    @click="$emit('click', $event)"
  >
    <font-awesome-icon :icon="icon" :size="size" :class="iconClass" />
  </sba-button>
</template>

<script lang="ts">
import { IconDefinition } from '@fortawesome/fontawesome-svg-core';
import { PropType, defineComponent } from 'vue';

import SbaButton from '@/components/sba-button';

export default defineComponent({
  components: { SbaButton },
  props: {
    title: {
      type: String,
      required: false,
      default: null,
    },
    icon: {
      type: Object as PropType<Array<string> | string | IconDefinition>,
      required: true,
    },
    size: {
      type: String,
      default: null,
    },
    iconClass: {
      type: String,
      default: null,
    },
  },
  emits: ['click'],
});
</script>

<style></style>
