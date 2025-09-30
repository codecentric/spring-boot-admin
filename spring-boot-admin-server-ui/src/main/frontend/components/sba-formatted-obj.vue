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
  <pre class="formatted" v-html="formatted" />
</template>

<script setup lang="ts">
import { computed } from 'vue';

import { createAutolink } from '@/utils/autolink';
import { objToYaml } from '@/utils/objToYaml';
import { sanitizeHtml } from '@/utils/sanitizeHtml';

const props = defineProps<{
  value: unknown;
}>();

const autolinkFn = createAutolink({
  truncate: {
    length: 50,
    location: 'smart',
  },
});

const formatted = computed(() => {
  const yaml = objToYaml(props.value);
  const yamlWithLinks = autolinkFn(yaml);
  return sanitizeHtml(yamlWithLinks);
});
</script>

<style scoped>
.formatted {
  @apply whitespace-break-spaces break-words;

  &:deep(a[href]) {
    @apply text-sba-500 underline font-bold;
  }
}
</style>
