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
      <div class="ml-2 transition-opacity" :class="{ 'opacity-0': !panelOpen }">
        ({{ Object.keys(info).length }})
      </div>
    </template>

    <sba-alert
      v-if="error"
      :error="error"
      class="border-l-4"
      :title="$t('term.fetch_failed')"
    />
    <div v-else class="content info -mx-4 -my-3">
      <table v-if="!isEmptyInfo" class="table table-full table-striped">
        <tr v-for="(value, key) in info" :key="key">
          <td class="info__key" v-text="key" />
          <td>
            <sba-formatted-obj :value="value" />
          </td>
        </tr>
      </table>
      <p
        v-else
        class="is-muted"
        v-text="$t('instances.details.info.no_info_provided')"
      />
    </div>
  </sba-accordion>
</template>

<script setup>
import { computed, ref, watch } from 'vue';

import SbaAlert from '@/components/sba-alert.vue';
import SbaFormattedObj from '@/components/sba-formatted-obj.vue';

import Instance from '@/services/instance';
import SbaAccordion from '@/views/instances/details/sba-accordion.vue';

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

const info = computed(() => liveInfo.value || props.instance.info);
const isEmptyInfo = computed(() => Object.keys(info.value).length <= 0);

async function fetchInfo() {
  if (props.instance.hasEndpoint('info')) {
    currentInstanceId.value = props.instance.id;
    loading.value = true;
    error.value = null;
    try {
      const res = await props.instance.fetchInfo();
      liveInfo.value = res.data;
    } catch (err) {
      error.value = err;

      console.warn('Fetching info failed:', err);
    } finally {
      loading.value = false;
    }
  }
}

function reloadInfo() {
  if (props.instance.id !== currentInstanceId.value) {
    fetchInfo();
  }
}

watch(
  () => props.instance,
  () => reloadInfo(),
  { immediate: true },
);

fetchInfo();
</script>

<style lang="css">
.info {
  overflow: auto;
}

.info__key {
  vertical-align: top;
}
</style>
