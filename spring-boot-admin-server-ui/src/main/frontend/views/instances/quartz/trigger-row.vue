<template>
  <!-- Main Trigger Row -->
  <tr
    :class="{ 'bg-gray-50': isOpen }"
    class="border-b border-gray-200 bg-white hover:bg-gray-50 cursor-pointer"
    @click="toggle"
  >
    <td class="px-6 py-4">
      <button class="text-gray-600 hover:text-gray-900 transition-colors">
        <font-awesome-icon
          :icon="isOpen ? 'chevron-down' : 'chevron-right'"
          class="h-4 w-4"
        />
      </button>
    </td>
    <td class="px-6 py-4 font-semibold text-gray-900">{{ trigger.name }}</td>
    <td class="px-6 py-4 text-xs text-gray-600">
      {{ trigger.description || '—' }}
    </td>
    <td class="px-6 py-4">
      <span
        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800"
      >
        {{ trigger.group }}
      </span>
    </td>
    <td class="px-6 py-4">
      <trigger-state-badge :state="trigger.state" />
    </td>
    <td class="px-6 py-4">
      <span
        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800"
      >
        {{ trigger.type }}
      </span>
    </td>
    <td class="px-6 py-4">
      <span
        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800"
      >
        {{ trigger.priority }}
      </span>
    </td>
    <td class="px-6 py-4 text-xs">
      <span v-if="trigger.nextFireTime" class="text-gray-700 font-medium">
        {{ formatTime(trigger.nextFireTime) }}
      </span>
      <span v-else class="text-gray-500">—</span>
    </td>
  </tr>

  <!-- Expanded Details Row -->
  <tr v-if="isOpen" class="bg-gray-50 border-b border-gray-200">
    <td colspan="8" class="px-6 py-6">
      <div class="space-y-6">
        <!-- Description -->
        <div
          v-if="trigger.description"
          class="bg-white border border-gray-200 rounded p-4"
        >
          <p class="font-medium text-gray-700 mb-1">
            {{ $t('instances.quartz.description') }}
          </p>
          <p class="text-sm text-gray-600">{{ trigger.description }}</p>
        </div>

        <!-- Timing and Basic Info -->
        <div class="grid grid-cols-2 gap-6">
          <!-- Timing Information -->
          <div>
            <h5
              class="mb-4 flex items-center gap-2 font-semibold text-gray-900"
            >
              <font-awesome-icon icon="clock" class="h-4 w-4" />
              {{ $t('instances.quartz.timing_information') }}
            </h5>
            <div class="space-y-3 text-sm">
              <div>
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.state') }}
                </p>
                <trigger-state-badge :state="trigger.state" />
              </div>
              <div v-if="trigger.previousFireTime">
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.last_fire_time') }}
                </p>
                <p class="text-gray-600">
                  {{ formatDateTime(trigger.previousFireTime) }}
                </p>
              </div>
              <div v-if="trigger.nextFireTime">
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.next_fire_time') }}
                </p>
                <p class="text-gray-700 font-medium">
                  {{ formatDateTime(trigger.nextFireTime) }}
                </p>
              </div>
              <div v-if="trigger.finalFireTime">
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.final_fire_time') }}
                </p>
                <p class="text-gray-600">
                  {{ formatDateTime(trigger.finalFireTime) }}
                </p>
              </div>
            </div>
          </div>

          <!-- Basic Information -->
          <div>
            <h5
              class="mb-4 flex items-center gap-2 font-semibold text-gray-900"
            >
              <font-awesome-icon icon="info-circle" class="h-4 w-4" />
              {{ $t('instances.quartz.basic_information') }}
            </h5>
            <div class="space-y-3 text-sm">
              <div>
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.name') }}
                </p>
                <p class="text-gray-600">{{ trigger.name }}</p>
              </div>
              <div>
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.group') }}
                </p>
                <p class="text-gray-600">{{ trigger.group }}</p>
              </div>
              <div>
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.type') }}
                </p>
                <span
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800"
                >
                  {{ trigger.type }}
                </span>
              </div>
              <div>
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.priority') }}
                </p>
                <p class="text-gray-600">{{ trigger.priority }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Schedule Details Based on Type -->
        <div v-if="triggerScheduleDetails">
          <div class="border-t border-gray-300 pt-4">
            <h5
              class="mb-4 flex items-center gap-2 font-semibold text-gray-900"
            >
              <font-awesome-icon icon="calendar" class="h-4 w-4" />
              {{ $t('instances.quartz.schedule_configuration') }}
            </h5>
            <component :is="triggerScheduleComponent" :trigger="trigger" />
          </div>
        </div>

        <!-- Time Window -->
        <div v-if="trigger.startTime || trigger.endTime">
          <div class="border-t border-gray-300 pt-4">
            <h5
              class="mb-4 flex items-center gap-2 font-semibold text-gray-900"
            >
              <font-awesome-icon icon="hourglass" class="h-4 w-4" />
              {{ $t('instances.quartz.time_window') }}
            </h5>
            <div class="grid grid-cols-2 gap-6 text-sm">
              <div>
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.start_time') }}
                </p>
                <p class="text-gray-600">
                  {{
                    trigger.startTime ? formatDateTime(trigger.startTime) : '—'
                  }}
                </p>
              </div>
              <div>
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.end_time') }}
                </p>
                <p class="text-gray-600">
                  {{ trigger.endTime ? formatDateTime(trigger.endTime) : '—' }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Calendar Association -->
        <div v-if="trigger.calendarName">
          <div class="border-t border-gray-300 pt-4">
            <h5
              class="mb-3 flex items-center gap-2 font-semibold text-gray-900"
            >
              <font-awesome-icon icon="calendar-check" class="h-4 w-4" />
              {{ $t('instances.quartz.calendar') }}
            </h5>
            <p class="text-sm text-gray-600">
              {{ $t('instances.quartz.associated_with_calendar') }}:
              <strong class="font-medium">{{ trigger.calendarName }}</strong>
            </p>
          </div>
        </div>

        <!-- Trigger Data -->
        <div v-if="trigger.data && Object.keys(trigger.data).length > 0">
          <div class="border-t border-gray-300 pt-4">
            <h5
              class="mb-3 flex items-center gap-2 font-semibold text-gray-900"
            >
              <font-awesome-icon icon="database" class="h-4 w-4" />
              {{ $t('instances.quartz.trigger_data') }}
            </h5>
            <div class="overflow-hidden rounded border border-gray-300">
              <table class="min-w-full text-sm">
                <thead>
                  <tr class="border-b border-gray-300 bg-gray-100">
                    <th class="px-4 py-2 text-left font-semibold text-gray-900">
                      {{ $t('instances.quartz.key') }}
                    </th>
                    <th class="px-4 py-2 text-left font-semibold text-gray-900">
                      {{ $t('instances.quartz.value') }}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="(value, key) in trigger.data"
                    :key="key"
                    class="border-b border-gray-200 hover:bg-gray-100"
                  >
                    <td class="px-4 py-2 font-medium text-gray-900">
                      {{ key }}
                    </td>
                    <td class="px-4 py-2 text-gray-600 font-mono text-xs">
                      {{ value }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </td>
  </tr>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

import TriggerStateBadge from './trigger-state-badge.vue';
import CalendarIntervalDetails from './trigger-types/calendar-interval-details.vue';
import CronTriggerDetails from './trigger-types/cron-details.vue';
import CustomTriggerDetails from './trigger-types/custom-details.vue';
import DailyTimeIntervalDetails from './trigger-types/daily-time-interval-details.vue';
import SimpleTriggerDetails from './trigger-types/simple-details.vue';

interface TriggerDetail {
  group: string;
  name: string;
  description?: string;
  state: string;
  type: 'cron' | 'simple' | 'dailyTimeInterval' | 'calendarInterval' | 'custom';
  priority: number;
  startTime?: string;
  endTime?: string;
  previousFireTime?: string;
  nextFireTime?: string;
  finalFireTime?: string;
  calendarName?: string;
  data?: Record<string, string>;
  cron?: { expression: string; timeZone?: string };
  simple?: { interval: number; repeatCount: number; timesTriggered: number };
  dailyTimeInterval?: {
    interval: number;
    daysOfWeek: number[];
    startTimeOfDay?: string;
    endTimeOfDay?: string;
    repeatCount: number;
    timesTriggered: number;
  };
  calendarInterval?: {
    interval: number;
    timeZone?: string;
    timesTriggered: number;
    preserveHourOfDayAcrossDaylightSavings: boolean;
    skipDayIfHourDoesNotExist: boolean;
  };
  custom?: { trigger: string };
  [key: string]: unknown;
}

interface Props {
  triggerDetail: TriggerDetail;
}

const props = defineProps<Props>();

const isOpen = ref(false);

const trigger = computed(() => props.triggerDetail);

const triggerScheduleDetails = computed(() =>
  Boolean(
    trigger.value.cron ||
    trigger.value.simple ||
    trigger.value.dailyTimeInterval ||
    trigger.value.calendarInterval ||
    trigger.value.custom,
  ),
);

const triggerScheduleComponent = computed(() => {
  const type = trigger.value.type;
  const componentMap: Record<string, any> = {
    cron: CronTriggerDetails,
    simple: SimpleTriggerDetails,
    dailyTimeInterval: DailyTimeIntervalDetails,
    calendarInterval: CalendarIntervalDetails,
    custom: CustomTriggerDetails,
  };
  return componentMap[type] || null;
});

const toggle = (): void => {
  isOpen.value = !isOpen.value;
};

const formatTime = (dateString?: string): string => {
  if (!dateString) return '—';
  try {
    const date = new Date(dateString);
    return date.toLocaleTimeString();
  } catch {
    return dateString;
  }
};

const formatDateTime = (dateString?: string): string => {
  if (!dateString) return '—';
  try {
    const date = new Date(dateString);
    return date.toLocaleString();
  } catch {
    return dateString;
  }
};
</script>
