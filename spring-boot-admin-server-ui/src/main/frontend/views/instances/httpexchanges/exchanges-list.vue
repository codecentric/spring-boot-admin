<!--
  - Copyright 2014-2020 the original author or authors.
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
  <DataTable
    v-model:expanded-rows="expandedRows"
    striped-rows
    data-key="key"
    size="small"
    :value="exchanges"
    paginator
    :rows="50"
    :rows-per-page-options="[5, 10, 20, 50, 100, 250, 500]"
  >
    <Column expander style="width: 2rem" />
    <template #expansion="slotProps">
      <code
        class="whitespace-pre-wrap text-sm"
        v-text="JSON.stringify(slotProps.data, null, 2)"
      />
    </template>

    <Column field="timestamp" :header="$t('instances.httpexchanges.timestamp')">
      <template #body="{ data }">
        <span class="whitespace-nowrap">
          {{ formatDateTime(data.timestamp) }}
        </span>
      </template>
    </Column>

    <Column
      field="request.uri"
      :header="$t('instances.httpexchanges.uri_parts')"
    >
      <template #body="{ data }">
        <div>
          <div class="whitespace-nowrap">
            {{ getLastPathSegment(data.request.uri) }}
          </div>
          <div class="text-xs text-gray-500 whitespace-nowrap">
            {{ getPrecedingPath(data.request.uri) }}
          </div>
        </div>
      </template>
    </Column>
    <Column
      field="request.uri"
      style="max-width: 200px"
      :header="$t('instances.httpexchanges.uri')"
    >
      <template #body="{ data }">
        <div
          v-tooltip.top="data.request.uri"
          class="overflow-hidden text-ellipsis whitespace-nowrap text-left"
          style="direction: rtl"
        >
          {{ data.request.uri }}
        </div>
      </template>
    </Column>
    <Column
      field="request.method"
      :header="$t('instances.httpexchanges.method')"
    />
    <Column field="status" :header="$t('instances.httpexchanges.status')">
      <template #body="{ data }">
        <code>{{ data.response.status }}</code>
        <div class="text-xs text-gray-500 whitespace-nowrap">
          {{ getHttpStatusText(data.response.status) }}
        </div>
      </template>
    </Column>
    <Column
      class="whitespace-nowrap"
      :header="$t('instances.httpexchanges.content')"
    >
      <template #body="{ data }">
        <ContentColumn :data="data" />
      </template>
    </Column>

    <Column field="time" :header="$t('instances.httpexchanges.time')">
      <template #body="{ data }">
        {{
          data.timeTaken
            ? `${toMilliseconds(parse(data.timeTaken)).toFixed(0)} ms`
            : ''
        }}
      </template>
    </Column>

    <template #empty>
      <div class="text-center" v-text="$t('instances.httpexchanges.no_data')" />
    </template>
  </DataTable>
</template>

<script setup lang="ts">
import { parse } from 'iso8601-duration';
import { Column, DataTable } from 'primevue';
import { ref } from 'vue';

import ContentColumn from './content-column.vue';

import { useDateTimeFormatter } from '@/composables/useDateTimeFormatter';
import { getHttpStatusText } from '@/utils/http-status';
import { toMilliseconds } from '@/utils/iso8601-duration';
import { Exchange } from '@/views/instances/httpexchanges/Exchange';

const { formatDateTime } = useDateTimeFormatter();

type Props = {
  exchanges?: Array<Exchange>;
};

withDefaults(defineProps<Props>(), {
  exchanges: () => [],
});

const expandedRows = ref([]);

const getLastPathSegment = (uri: string): string => {
  try {
    const url = new URL(uri);
    const pathSegments = url.pathname.split('/').filter((segment) => segment);
    return pathSegments.length > 0
      ? pathSegments[pathSegments.length - 1]
      : '/';
  } catch {
    // If URL parsing fails, try to extract from path directly
    const pathSegments = uri.split('/').filter((segment) => segment);
    return pathSegments.length > 0
      ? pathSegments[pathSegments.length - 1]
      : '/';
  }
};

const getPrecedingPath = (uri: string): string => {
  try {
    const url = new URL(uri);
    const pathSegments = url.pathname.split('/').filter((segment) => segment);
    if (pathSegments.length <= 1) return '';
    return '/' + pathSegments.slice(0, -1).join('/');
  } catch {
    // If URL parsing fails, try to extract from path directly
    const pathSegments = uri.split('/').filter((segment) => segment);
    if (pathSegments.length <= 1) return '';
    return '/' + pathSegments.slice(0, -1).join('/');
  }
};
</script>
