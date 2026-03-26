<template>
  <div v-if="hasTriggers">
    <!-- Header -->
    <div class="mb-4 flex items-center justify-between">
      <h2 class="flex items-center gap-3 text-2xl font-bold text-gray-900">
        <font-awesome-icon icon="clock" class="h-6 w-6 text-green-600" />
        {{ $t('instances.quartz.triggers') }}
      </h2>
      <div class="flex items-center gap-2 rounded-full bg-gray-100 px-3 py-1">
        <span class="text-sm font-semibold text-gray-900">
          {{ triggers.length }}
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
            <th
              class="px-6 py-3 text-left font-semibold text-gray-900 w-5"
            ></th>
            <th class="px-6 py-3 text-left font-semibold text-gray-900 w-1/5">
              {{ $t('instances.quartz.name') }}
            </th>
            <th class="px-6 py-3 text-left font-semibold text-gray-900 w-1/5">
              {{ $t('instances.quartz.description') }}
            </th>
            <th class="px-6 py-3 text-left font-semibold text-gray-900 w-1/6">
              {{ $t('instances.quartz.group') }}
            </th>
            <th class="px-6 py-3 text-left font-semibold text-gray-900 w-1/6">
              {{ $t('instances.quartz.state') }}
            </th>
            <th class="px-6 py-3 text-left font-semibold text-gray-900 w-1/6">
              {{ $t('instances.quartz.type') }}
            </th>
            <th class="px-6 py-3 text-left font-semibold text-gray-900 w-1/12">
              {{ $t('instances.quartz.priority') }}
            </th>
            <th class="px-6 py-3 text-left font-semibold text-gray-900 w-1/6">
              {{ $t('instances.quartz.next_fire_time') }}
            </th>
          </tr>
        </thead>

        <trigger-row
          v-for="trigger in triggers"
          :key="getTriggerKey(trigger)"
          :trigger-detail="trigger"
        />
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

import TriggerRow from './trigger-row.vue';

interface Trigger {
  group: string;
  name: string;
  [key: string]: unknown;
}

interface Props {
  triggers: Trigger[];
}

const props = defineProps<Props>();

const hasTriggers = computed(() => props.triggers && props.triggers.length > 0);

const getTriggerKey = (trigger: Trigger): string =>
  `${trigger.group}-${trigger.name}`;
</script>
