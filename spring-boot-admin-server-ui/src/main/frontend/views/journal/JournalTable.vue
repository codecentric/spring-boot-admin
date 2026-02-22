<!--
  - Copyright 2014-2025 the original author or authors.
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
    v-model:filters="filters"
    v-model:expanded-rows="expandedRows"
    striped-rows
    :value="mappedEvents"
    paginator
    sort-field="date"
    :sort-order="-1"
    :rows="50"
    :rows-per-page-options="[5, 10, 20, 50, 100, 250, 500]"
    filter-display="menu"
    :global-filter-fields="['application', 'instance', 'type']"
  >
    <template #header>
      <div class="flex justify-between">
        <Button
          type="button"
          icon="pi pi-filter-slash"
          :label="t('term.filter_action.reset')"
          variant="outlined"
          @click="clearFilter()"
        />

        <IconField>
          <InputIcon>
            <font-awesome-icon :icon="faSearch" />
          </InputIcon>
          <InputText
            v-model="filters.global.value"
            :placeholder="t('term.keyword_search')"
          />
        </IconField>
      </div>
    </template>

    <Column expander style="width: 2rem" />

    <Column
      :header="$t('term.application')"
      :sortable="true"
      field="application"
      :show-filter-match-modes="false"
      filter-field="application"
      :filter-menu-style="{
        minWidth: '16rem',
      }"
    >
      <template #body="{ data }">
        {{ data.application }}
      </template>
      <template #filter="{ filterModel }">
        <MultiSelect
          v-model="filterModel.value"
          :options="applicationNames"
          :placeholder="t('journal.filter.application.any')"
          :show-toggle-all="false"
          :max-selected-labels="1"
        >
          <template #option="slotProps">
            <div class="flex items-center gap-2">
              <span>{{ slotProps.option }}</span>
            </div>
          </template>
        </MultiSelect>
      </template>
    </Column>

    <Column
      :header="$t('term.instance')"
      :sortable="true"
      field="instance"
      :show-filter-match-modes="false"
      filter-field="instance"
      :filter-menu-style="{
        minWidth: '16rem',
      }"
      class="w-48"
    >
      <template #body="{ data }">
        <router-link
          :to="{
            name: 'instances/details',
            params: { instanceId: data.instance },
          }"
        >
          {{ data.instance }}
        </router-link>
      </template>
      <template #filter="{ filterModel }">
        <MultiSelect
          v-model="filterModel.value"
          :options="instanceIds"
          :placeholder="t('journal.filter.instance_id.any')"
          :show-toggle-all="false"
          :max-selected-labels="1"
        >
          <template #option="slotProps">
            <div class="flex items-center gap-2">
              <span>{{ slotProps.option }}</span>
            </div>
          </template>
        </MultiSelect>
      </template>
    </Column>

    <Column
      :header="$t('term.time')"
      :sortable="true"
      field="date"
      filter-field="date"
      data-type="date"
      :filter-menu-style="{
        minWidth: '16rem',
      }"
      class="w-72"
    >
      <template #body="{ data }">
        {{ formatDateTime(data.date) }}
      </template>
      <template #filter="{ filterModel }">
        <DatePicker v-model="filterModel.value" :manual-input="false" />
      </template>
    </Column>

    <Column
      :header="$t('term.event')"
      :sortable="true"
      field="type"
      :show-filter-match-modes="false"
      filter-field="type"
      :filter-menu-style="{
        minWidth: '16rem',
      }"
      class="w-1/4"
    >
      <template #body="{ data }">
        {{ data.type }}
      </template>
      <template #filter="{ filterModel }">
        <MultiSelect
          v-model="filterModel.value"
          :options="eventTypes"
          :placeholder="t('journal.filter.event_type.any')"
          :show-toggle-all="false"
          :max-selected-labels="1"
        >
          <template #option="slotProps">
            <div class="flex items-center gap-2">
              <span>{{ slotProps.option }}</span>
            </div>
          </template>
        </MultiSelect>
      </template>
    </Column>
    <template #expansion="slotProps">
      <pre class="whitespace-pre-wrap text-sm">{{
        JSON.stringify(slotProps, null, 2)
      }}</pre>
    </template>
  </DataTable>
</template>

<script setup lang="ts">
import { faSearch } from '@fortawesome/free-solid-svg-icons/faSearch';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { FilterMatchMode, FilterOperator } from '@primevue/core/api';
import {
  Button,
  Column,
  DataTable,
  DatePicker,
  IconField,
  InputIcon,
  InputText,
  MultiSelect,
} from 'primevue';
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import { useDateTimeFormatter } from '@/composables/useDateTimeFormatter';
import Application from '@/services/application';
import { pushFlattened } from '@/utils/array';
import { InstanceEvent } from '@/views/journal/InstanceEvent';
import {
  getApplicationNames,
  getApplicationNamesByInstanceId,
} from '@/views/journal/utils';

const route = useRoute();
const router = useRouter();

const { events = [] } = defineProps<{
  events?: Array<InstanceEvent>;
  applications: Array<Application>;
}>();

const { formatDateTime } = useDateTimeFormatter();
const { t } = useI18n();
const expandedRows = ref([]);

const applicationNames = computed(() => {
  return getApplicationNames(events);
});
const applicationNamesByInstanceId = computed(() => {
  return getApplicationNamesByInstanceId(events);
});
const mappedEvents = computed(() => {
  return events.map((e) => ({
    ...e,
    date: new Date(e.timestamp),
    application: applicationNamesByInstanceId.value[e.instance],
  }));
});

const eventTypes = computed(() => {
  return [...new Set(mappedEvents.value.map((e) => e.type))].sort((a, b) =>
    a.localeCompare(b),
  );
});

const instanceIds = computed(() => {
  return [...new Set(mappedEvents.value.map((e) => e.instance))].sort((a, b) =>
    a.localeCompare(b),
  );
});

const filters = ref();
const initFilters = () => {
  filters.value = {
    global: { value: null, matchMode: FilterMatchMode.CONTAINS },
    application: {
      value: null,
      matchMode: FilterMatchMode.IN,
    },
    instance: {
      value: null,
      matchMode: FilterMatchMode.IN,
    },
    date: {
      operator: FilterOperator.AND,
      constraints: [{ value: null, matchMode: FilterMatchMode.DATE_IS }],
    },
    type: {
      value: null,
      matchMode: FilterMatchMode.IN,
    },
  };
};
initFilters();

const clearFilter = () => {
  initFilters();
};

watch(
  filters,
  () => {
    const query: Record<string, string | string[]> = {};
    if (filters.value.global.value) {
      query.q = filters.value.global.value;
    }
    if (filters.value.application?.value?.length) {
      query.application = filters.value.application.value;
    }
    if (filters.value.instance?.value?.length) {
      query.instanceId = filters.value.instance.value;
    }
    if (filters.value.type?.value?.length) {
      query.type = filters.value.type.value;
    }
    router.push({ query });
  },
  { deep: true },
);

onMounted(() => {
  filters.value.global.value = route.query.q || null;
  filters.value.application.value = pushFlattened([], route.query.application);
  filters.value.instance.value = pushFlattened([], route.query.instanceId);
  filters.value.type.value = pushFlattened([], route.query.type);
});
</script>
