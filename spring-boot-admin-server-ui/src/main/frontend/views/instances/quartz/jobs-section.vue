<template>
  <div v-if="hasJobs" class="mb-8">
    <!-- Header -->
    <div class="mb-4 flex items-center justify-between">
      <h2 class="flex items-center gap-3 text-2xl font-bold text-gray-900">
        <font-awesome-icon icon="briefcase" class="h-6 w-6 text-blue-600" />
        {{ $t('instances.quartz.jobs') }}
      </h2>
      <div class="flex items-center gap-2 rounded-full bg-gray-100 px-3 py-1">
        <span class="text-sm font-semibold text-gray-900">
          {{ jobs.length }}
        </span>
        <span class="text-xs font-medium text-gray-600">
          {{ $t('instances.quartz.total') }}
        </span>
      </div>
    </div>

    <!-- Table -->
    <div class="overflow-hidden rounded-lg border border-gray-200 shadow-sm">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-gray-200 bg-gray-50">
            <th class="px-6 py-3 text-left font-semibold text-gray-900 w-1/3">
              {{ $t('instances.quartz.name') }}
            </th>
            <th class="px-6 py-3 text-left font-semibold text-gray-900 w-1/4">
              {{ $t('instances.quartz.description') }}
            </th>
            <th class="px-6 py-3 text-left font-semibold text-gray-900 w-1/6">
              {{ $t('instances.quartz.group') }}
            </th>
            <th class="px-6 py-3 text-left font-semibold text-gray-900 w-1/12">
              {{ $t('instances.quartz.durable') }}
            </th>
            <th class="px-6 py-3 text-left font-semibold text-gray-900 w-1/6">
              {{ $t('instances.quartz.recovery') }}
            </th>
          </tr>
        </thead>
        <tbody>
          <template v-for="job in jobs" :key="getJobKey(job)">
            <job-row
              :job-detail="job"
              :is-expanded="selectedJobKey === getJobKey(job)"
              @toggle="toggleJobExpanded(job)"
            />
          </template>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

import JobRow from './job-row.vue';

interface Job {
  group: string;
  name: string;
  [key: string]: unknown;
}

interface Props {
  jobs: Job[];
}

const props = defineProps<Props>();

const selectedJobKey = ref<string | null>(null);

const hasJobs = computed(() => props.jobs && props.jobs.length > 0);

const getJobKey = (job: Job): string => `${job.group}-${job.name}`;

const toggleJobExpanded = (job: Job): void => {
  const key = getJobKey(job);
  selectedJobKey.value = selectedJobKey.value === key ? null : key;
};
</script>
