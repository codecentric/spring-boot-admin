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
        {{ formatDateTime(data.timestamp) }}
      </template>
    </Column>
    <Column
      field="request.method"
      :header="$t('instances.httpexchanges.method')"
    />
    <Column field="request.uri" :header="$t('instances.httpexchanges.uri')" />
    <Column field="status" :header="$t('instances.httpexchanges.status')">
      <template #body="{ data }">
        <code v-text="data.response ? data.response.status : 'pending'" />
      </template>
    </Column>
    <Column
      field="contentTypeRequest"
      :header="$t('instances.httpexchanges.content_type_request')"
    />
    <Column
      field="contentLengthRequest"
      :header="$t('instances.httpexchanges.length_request')"
    >
      <template #body="{ data }">
        {{
          data.contentLengthRequest
            ? prettyBytes(data.contentLengthRequest)
            : ''
        }}
      </template>
    </Column>
    <Column
      field="contentTypeResponse"
      :header="$t('instances.httpexchanges.content_type_response')"
    />
    <Column
      field="contentLengthResponse"
      :header="$t('instances.httpexchanges.length_response')"
    >
      <template #body="{ data }">
        {{
          data.contentLengthResponse
            ? prettyBytes(data.contentLengthResponse)
            : ''
        }}
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
  </DataTable>
</template>

<script setup lang="ts">
import { parse } from 'iso8601-duration';
import prettyBytes from 'pretty-bytes';
import { Column, DataTable } from 'primevue';
import { ref } from 'vue';

import { useDateTimeFormatter } from '@/composables/useDateTimeFormatter';
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
</script>
