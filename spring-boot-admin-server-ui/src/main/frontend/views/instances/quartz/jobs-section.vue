<template>
  <div v-if="hasJobs" class="mb-8">
    <!-- Header -->
    <div
      class="mb-4 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between"
    >
      <h2
        class="flex items-center gap-3 text-xl sm:text-2xl font-bold text-gray-900"
      >
        <font-awesome-icon
          icon="briefcase"
          class="h-5 w-5 sm:h-6 sm:w-6 text-blue-600"
        />
        {{ $t('instances.quartz.jobs') }}
      </h2>
      <div
        class="flex items-center gap-2 rounded-full bg-gray-100 px-3 py-1 w-fit"
      >
        <span class="text-sm font-semibold text-gray-900">
          {{ jobs.length }}
        </span>
        <span class="text-xs font-medium text-gray-600">
          {{ $t('instances.quartz.total') }}
        </span>
      </div>
    </div>

    <!-- Table - responsive with horizontal scroll on mobile -->
    <div class="overflow-x-auto rounded-lg border border-gray-200 shadow-sm">
      <table class="w-full text-xs sm:text-sm">
        <thead class="hidden sm:table-header-group">
          <tr class="border-b border-gray-200 bg-gray-50">
            <th class="px-3 sm:px-6 py-3 text-left font-semibold text-gray-900">
              {{ $t('instances.quartz.name') }}
            </th>
            <th class="px-3 sm:px-6 py-3 text-left font-semibold text-gray-900">
              {{ $t('instances.quartz.description') }}
            </th>
            <th class="px-3 sm:px-6 py-3 text-left font-semibold text-gray-900">
              {{ $t('instances.quartz.group') }}
            </th>
            <th class="px-3 sm:px-6 py-3 text-left font-semibold text-gray-900">
              {{ $t('instances.quartz.durable') }}
            </th>
            <th class="px-3 sm:px-6 py-3 text-left font-semibold text-gray-900">
              {{ $t('instances.quartz.recovery') }}
            </th>
          </tr>
        </thead>
        <tbody>
          <template v-for="job in jobs" :key="getJobKey(job)">
            <job-row
              :job-detail="job"
              :is-expanded="selectedJobKey === getJobKey(job)"
              :instance="instance"
              @toggle="toggleJobExpanded(job)"
              @action="handleAction"
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

import Instance from '@/services/instance';

interface Job {
  group: string;
  name: string;
  [key: string]: unknown;
}

interface Props {
  jobs: Job[];
  instance: Instance;
}

const props = defineProps<Props>();

const selectedJobKey = ref<string | null>(null);

const hasJobs = computed(() => props.jobs && props.jobs.length > 0);

const getJobKey = (job: Job): string => `${job.group}-${job.name}`;

const toggleJobExpanded = (job: Job): void => {
  const key = getJobKey(job);
  selectedJobKey.value = selectedJobKey.value === key ? null : key;
};

const handleAction = (action: string, success: boolean): void => {
  // Handle action feedback (can be connected to toast notifications later)
  if (!success) {
    console.warn(`Job action '${action}' failed`);
  }
};
</script>
