<template>
  <DataTable
    v-model:filters="filters"
    v-model:expanded-rows="expandedRows"
    striped-rows
    :value="events"
    paginator
    :rows="50"
    :rows-per-page-options="[5, 10, 20, 50, 100, 250, 500]"
    filter-display="row"
  >
    <Column expander style="width: 2rem" />
    <Column
      :header="$t('term.application')"
      filter-field="application.name"
      :show-filter-menu="false"
    >
      <template #body="{ data }">
        {{ data.application?.name }}
      </template>
      <template #filter="{ filterModel, filterCallback }">
        <MultiSelect
          v-model="filterModel.value"
          :options="applicationNames"
          :placeholder="t('journal.filter.application.any')"
          :show-toggle-all="false"
          max-selected-labels="1"
          @change="filterCallback()"
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
      filter-field="instance"
      :show-filter-menu="false"
    >
      <template #body="{ data }">
        {{ data.instance }}
      </template>
      <template #filter="{ filterModel, filterCallback }">
        <MultiSelect
          v-model="filterModel.value"
          :options="instanceIds"
          :placeholder="t('journal.filter.instance_id.any')"
          :show-toggle-all="false"
          max-selected-labels="1"
          @change="filterCallback()"
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
      sortable
      field="timestamp"
      filter-field="timestamp"
      data-type="date"
    >
      <template #body="{ data }">
        {{ formatDateTime(data.timestamp) }}
      </template>
      <template #filter="{ filterModel }">
        <DatePicker
          v-model="filterModel.value"
          show-time
          show-button-bar
          hour-format="24"
          date-format="mm/dd/yy"
          placeholder="mm/dd/yyyy HH:mm:ss"
        />
      </template>
    </Column>
    <Column
      :header="$t('term.event')"
      filter-field="type"
      :show-filter-menu="false"
    >
      <template #body="{ data }">
        {{ data.type }}
      </template>
      <template #filter="{ filterModel, filterCallback }">
        <MultiSelect
          v-model="filterModel.value"
          :options="eventTypes"
          :placeholder="t('journal.filter.event_type.any')"
          :show-toggle-all="false"
          max-selected-labels="1"
          @change="filterCallback()"
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
import { FilterMatchMode, FilterOperator } from '@primevue/core/api';
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';

import { useDateTimeFormatter } from '@/composables/useDateTimeFormatter';
import Application from '@/services/application';
import { InstanceEvent } from '@/views/journal/InstanceEvent';

const props = defineProps<{
  events: Array<InstanceEvent>;
  applications: Array<Application>;
}>();
const { formatDateTime } = useDateTimeFormatter();
const { t } = useI18n();

const events = computed(() => {
  return props.events.map((e) => ({
    ...e,
    application: getApplication(e.instance),
  }));
});

const eventTypes = computed(() => {
  return [...new Set(props.events.map((e) => e.type))].sort((a, b) =>
    a.localeCompare(b),
  );
});

const instanceIds = computed(() => {
  return [...new Set(props.events.map((e) => e.instance))].sort((a, b) =>
    a.localeCompare(b),
  );
});

const instanceToApplicationMap = computed(() => {
  return instanceIds.value.reduce((prev, instanceId) => {
    return {
      ...prev,
      [instanceId]: getApplication(instanceId),
    };
  }, {});
});

const applicationNames = computed(() => {
  const names = Object.values(instanceToApplicationMap.value).map(
    (a) => a?.name,
  );
  return [...new Set(names)].sort((a, b) => a.localeCompare(b));
});

const filters = ref({
  'application.name': { value: null, matchMode: FilterMatchMode.IN },
  instance: { value: null, matchMode: FilterMatchMode.IN },
  timestamp: {
    operator: FilterOperator.AND,
    constraints: [{ value: null, matchMode: FilterMatchMode.DATE_IS }],
  },
  type: { value: null, matchMode: FilterMatchMode.IN },
});

const expandedRows = ref({});

function getApplication(instanceId: string) {
  return props.applications.find((application) =>
    application.findInstance(instanceId),
  );
}
</script>
