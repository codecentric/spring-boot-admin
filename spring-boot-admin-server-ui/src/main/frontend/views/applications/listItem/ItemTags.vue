<!--
  - Copyright 2014-2024 the original author or authors.
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
  <div
    v-if="Object.keys(instance.tags ?? {}).length > 0"
    class="mt-2 hidden lg:block overflow-x-auto"
  >
    <sba-tags :small="true" :tags="instance.tags" />
  </div>
</template>

<script setup lang="ts">
import SbaTags from '@/components/sba-tags.vue';

import Instance from '@/services/instance';

defineProps<{
  instance: Instance;
}>();
</script>
