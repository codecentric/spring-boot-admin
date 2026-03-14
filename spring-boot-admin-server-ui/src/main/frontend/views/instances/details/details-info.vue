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
    :loading="loading"
  >
    <template #title>
      <div class="ml-2 transition-opacity" :class="{ 'opacity-0': panelOpen }">
        ({{ Object.keys(info).length }})
      </div>
    </template>

    <sba-alert
      v-if="error"
      :error="error"
      class="border-l-4"
      :title="$t('term.fetch_failed')"
    />

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
import { computed, ref, watch } from 'vue';

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
const error = ref(null);
const loading = ref(false);
const liveInfo = ref(null);
const currentInstanceId = ref(null);
const currentInstanceUpdateKey = ref(null);
const requestGen = ref(0);

const info = computed(() => formatInfo(liveInfo.value || props.instance.info));
const isEmptyInfo = computed(() => Object.keys(info.value).length <= 0);

async function fetchInfo() {
  if (props.instance.hasEndpoint('info')) {
    const gen = ++requestGen.value;

    currentInstanceId.value = props.instance.id;
    currentInstanceUpdateKey.value =
      props.instance.version ?? props.instance.statusTimestamp ?? props.instance.id;
    loading.value = true;
    error.value = null;

    try {
      const res = await props.instance.fetchInfo();
      // Verify this is still the latest request generation
      if (gen !== requestGen.value) {
        return;
      }
      liveInfo.value = res.data;
    } catch (err) {
      // Stale error - ignore if a newer request was initiated
      if (gen !== requestGen.value) {
        return;
      }
      error.value = err;
      console.warn('Fetching info failed:', err);
    } finally {
      // Don't clear loading state if new request started
      if (gen !== requestGen.value) {
        return;
      }
      loading.value = false;
    }
  }
}

function reloadInfo() {
  // Increment generation to invalidate any in-flight requests
  requestGen.value++;
  fetchInfo();
}

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

watch(
  () => [
    props.instance.id,
    props.instance.version ?? props.instance.statusTimestamp,
  ],
  () => reloadInfo(),
  { immediate: true },
);
</script>

<style lang="css">
.info {
  overflow: auto;
}

.info__key {
  vertical-align: top;
}
</style>
