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
    v-if="hasLoaded"
    :id="`process-details-panel__${instance.id}`"
    v-model="panelOpen"
    :title="$t('instances.details.process.title')"
  >
    <template #title>
      <div
        class="ml-2 text-xs font-mono transition-opacity flex-1 justify-items-end"
        :class="{ 'opacity-0': panelOpen }"
      >
        <ul class="flex gap-4">
          <li>
            <span class="block 2xl:inline">
              {{ t('instances.details.process.uptime_short') }}:
            </span>
            <process-uptime :value="tableData.uptime.value" />
          </li>
          <li>
            <span class="block 2xl:inline">
              {{ t('instances.details.process.system_cpu_usage_short') }}:
            </span>
            {{ tableData.systemCpuLoad.value }}
          </li>
          <li>
            <span class="block 2xl:inline">
              {{ t('instances.details.process.process_cpu_usage_short') }}:
            </span>
            {{ tableData.processCpuLoad.value }}
          </li>
        </ul>
      </div>
    </template>
    <div>
      <sba-alert v-if="error" :error="error" :title="$t('term.fetch_failed')" />
      <div v-else class="-mx-4 -my-3">
        <sba-key-value-table :map="tableData" skip-null-values>
          <template #uptime="value">
            <process-uptime :value="value.value" />
          </template>
        </sba-key-value-table>
      </div>
    </div>
  </sba-accordion>
</template>

<script setup lang="ts">
import { Subscription } from 'rxjs';
import { take } from 'rxjs/operators';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';

import SbaAccordion from '@/components/sba-accordion.vue';

import sbaConfig from '@/sba-config';
import Instance from '@/services/instance';
import { concatMap, delay, retryWhen, timer } from '@/utils/rxjs';
import processUptime from '@/views/instances/details/process-uptime';
import { toMillis } from '@/views/instances/metrics/metric';

// Typdefinitionen

// Typdefinitionen
interface UptimeData {
  value: number | null;
  baseUnit: string | null;
}

interface MetricResponse {
  measurements: Array<{ value: number }>;
  baseUnit: string;
}

interface CpuLoadMetrics {
  processCpuLoad: number | null;
  systemCpuLoad: number | null;
}

interface TableData {
  label: string;
  value: number | string | null;
}

interface TableDataMap {
  pid: TableData;
  parentPid: TableData;
  owner: TableData;
  uptime: TableData;
  processCpuLoad: TableData;
  systemCpuLoad: TableData;
  cpus: TableData;
}

// Props Definition
const props = defineProps<{
  instance: Instance;
}>();

const { t, locale } = useI18n();

// Reaktive Zustandsvariablen
const hasLoaded = ref<boolean>(false);
const error = ref<Error | null>(null);

const pid = ref<number | null>(null);
const parentPid = ref<number | null>(null);
const owner = ref<string | null>(null);
const uptime = ref<UptimeData>({ value: null, baseUnit: null });
const systemCpuLoad = ref<number | null>(null);
const processCpuLoad = ref<number | null>(null);
const systemCpuCount = ref<number | null>(null);
const panelOpen = ref<boolean>(true);

// Berechnete Eigenschaften
const tableData = computed<TableDataMap>(() => {
  const formatNumber = Intl.NumberFormat(locale.value, {
    maximumFractionDigits: 2,
  });

  return {
    pid: {
      label: t('instances.details.process.pid'),
      value: pid.value,
    },
    parentPid: {
      label: t('instances.details.process.parent_pid'),
      value: parentPid.value,
    },
    owner: {
      label: t('instances.details.process.owner'),
      value: owner.value,
    },
    uptime: {
      label: t('instances.details.process.uptime'),
      value: toMillis(uptime.value.value, uptime.value.baseUnit),
    },
    processCpuLoad: {
      label: t('instances.details.process.process_cpu_usage'),
      value: isNaN(processCpuLoad.value)
        ? '-'
        : `${formatNumber.format(processCpuLoad.value * 100)}%`,
    },
    systemCpuLoad: {
      label: t('instances.details.process.system_cpu_usage'),
      value: isNaN(systemCpuLoad.value)
        ? '-'
        : `${formatNumber.format(systemCpuLoad.value * 100)}%`,
    },
    cpus: {
      label: t('instances.details.process.cpus'),
      value: systemCpuCount.value,
    },
  };
});

// Hilfsfunktionen
const fetchMetric = async (name: string): Promise<MetricResponse> => {
  const response = await props.instance.fetchMetric(name);
  return response.data;
};

const fetchUptime = async (): Promise<void> => {
  try {
    const response = await fetchMetric('process.uptime');
    uptime.value = {
      value: response.measurements[0].value,
      baseUnit: response.baseUnit,
    };
  } catch (err) {
    error.value = err instanceof Error ? err : new Error('Unknown error');
    console.warn('Fetching Uptime failed:', err);
  }
};

const fetchPid = async (): Promise<void> => {
  if (props.instance.hasEndpoint('env')) {
    try {
      const response = await props.instance.fetchEnv('PID');
      pid.value = response.data.property.value;
    } catch (err) {
      console.warn('Fetching PID failed:', err);
    }
  }
};

const fetchCpuCount = async (): Promise<void> => {
  try {
    const response = await fetchMetric('system.cpu.count');
    systemCpuCount.value = response.measurements[0].value;
  } catch (err) {
    console.warn('Fetching Cpu Count failed:', err);
  }
};

const fetchProcessInfo = async (): Promise<void> => {
  try {
    const response = await props.instance.fetchInfo();
    const processInfo = response.data.process;
    if (processInfo) {
      pid.value = processInfo.pid;
      parentPid.value = processInfo.parentPid;
      owner.value = processInfo.owner;
    }
  } catch (err) {
    console.warn('Fetching Process Info failed:', err);
  }
};

const fetchCpuLoadMetrics = async (): Promise<CpuLoadMetrics> => {
  const fetchProcessCpuLoad = fetchMetric('process.cpu.usage');
  const fetchSystemCpuLoad = fetchMetric('system.cpu.usage');

  let processCpuLoadValue: number | null = null;
  let systemCpuLoadValue: number | null = null;

  try {
    const response = await fetchProcessCpuLoad;
    processCpuLoadValue = response.measurements[0].value;
  } catch (err) {
    console.warn('Fetching Process CPU Load failed:', err);
  }

  try {
    const response = await fetchSystemCpuLoad;
    systemCpuLoadValue = response.measurements[0].value;
  } catch (err) {
    console.warn('Fetching System CPU Load failed:', err);
  }

  return {
    processCpuLoad: processCpuLoadValue,
    systemCpuLoad: systemCpuLoadValue,
  };
};

let subscription: Subscription;
onMounted(async () => {
  try {
    await Promise.allSettled([
      fetchPid(),
      fetchUptime(),
      fetchCpuCount(),
      fetchProcessInfo(),
    ]);
  } finally {
    hasLoaded.value = true;
  }

  subscription = timer(0, sbaConfig.uiSettings.pollTimer.process)
    .pipe(
      concatMap(fetchCpuLoadMetrics),
      retryWhen((err) => err.pipe(delay(1000, take(5)))),
    )
    .subscribe({
      next: (data: CpuLoadMetrics) => {
        processCpuLoad.value = data.processCpuLoad;
        systemCpuLoad.value = data.systemCpuLoad;
      },
      error: (err: Error) => {
        hasLoaded.value = true;
        console.warn('Fetching CPU Usage metrics failed:', err);
        error.value = err;
      },
    });
});

onUnmounted(() => {
  subscription.unsubscribe();
});
</script>
