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
  <sba-accordion
    :id="`metadata-details-panel__${instance.id}`"
    v-model="panelOpen"
    :title="$t('instances.details.metadata.title')"
    :seamless="true"
  >
    <template #title>
      <div class="ml-2 transition-opacity" :class="{ 'opacity-0': panelOpen }">
        ({{ Object.keys(metadata).length }})
      </div>
    </template>
    <sba-key-value-table v-if="hasMetadata" :map="metadata" />
    <p
      v-else
      class="mx-4 my-3"
      v-text="$t('instances.details.metadata.no_data_provided')"
    />
  </sba-accordion>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';

import SbaAccordion from '@/components/sba-accordion.vue';

import Instance from '@/services/instance';
import { sortObject } from '@/utils/sortObject';

const { instance } = defineProps<{
  instance: Instance;
}>();

const panelOpen = ref(true);

const metadata = computed(() => {
  return sortObject(instance.registration.metadata);
});

const hasMetadata = computed(() => {
  return Object.keys(metadata.value).length > 0;
});
</script>
