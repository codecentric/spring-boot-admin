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
    :id="`info-details-panel__${instance.id}`"
    v-model="panelOpen"
    :title="$t('instances.details.info.title')"
  >
    <template #title>
      <div class="ml-2 transition-opacity" :class="{ 'opacity-0': panelOpen }">
        ({{ Object.keys(info).length }})
      </div>
    </template>

    <div class="content info -mx-4 -my-3">
      <sba-key-value-table v-if="!isEmptyInfo" :map="info" />
      <p
        v-else
        class="mx-4 my-3"
        v-text="$t('instances.details.info.no_info_provided')"
      />
    </div>
  </sba-accordion>
</template>

<script setup>
import { computed, ref } from 'vue';

import SbaAccordion from '@/components/sba-accordion.vue';
import SbaKeyValueTable from '@/components/sba-key-value-table.vue';

import Instance from '@/services/instance';
import { formatWithDataTypes } from '@/utils/formatWithDataTypes';

const props = defineProps({
  instance: {
    type: Instance,
    required: true,
  },
});

const panelOpen = ref(true);

const info = computed(() => formatInfo(props.instance.info));
const isEmptyInfo = computed(() => Object.keys(info.value).length <= 0);

function formatInfo(info) {
  return formatWithDataTypes(info, {
    'build.time': 'date',
    'process.memory.heap.committed': 'bytes',
    'process.memory.heap.init': 'bytes',
    'process.memory.heap.max': 'bytes',
    'process.memory.heap.used': 'bytes',
    'process.memory.nonHeap.committed': 'bytes',
    'process.memory.nonHeap.init': 'bytes',
    'process.memory.nonHeap.max': 'bytes',
    'process.memory.nonHeap.used': 'bytes',
  });
}
</script>

<style lang="css">
@reference "../../../index.css";
.info {
  overflow: auto;
}

.info__key {
  vertical-align: top;
}
</style>
