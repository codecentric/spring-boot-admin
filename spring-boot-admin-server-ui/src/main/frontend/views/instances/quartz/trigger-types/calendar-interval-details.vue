<template>
  <div class="space-y-6">
    <div class="grid grid-cols-4 gap-6 text-sm">
      <div>
        <p class="font-medium text-gray-700">
          {{ $t('instances.quartz.interval') }}
        </p>
        <p class="mt-1 text-gray-600">
          {{ formatInterval(trigger.calendarInterval.interval) }}
        </p>
      </div>
      <div>
        <p class="font-medium text-gray-700">
          {{ $t('instances.quartz.time_zone') }}
        </p>
        <p class="mt-1 text-gray-600">
          {{
            trigger.calendarInterval.timeZone ||
            $t('instances.quartz.system_default')
          }}
        </p>
      </div>
      <div>
        <p class="font-medium text-gray-700">
          {{ $t('instances.quartz.times_triggered') }}
        </p>
        <p class="mt-1 text-gray-600">
          {{ trigger.calendarInterval.timesTriggered }}
        </p>
      </div>
      <div>
        <p class="font-medium text-gray-700">
          {{ $t('instances.quartz.preserve_hour_of_day') }}
        </p>
        <p class="mt-1 flex items-center gap-2">
          <font-awesome-icon
            :icon="
              trigger.calendarInterval.preserveHourOfDayAcrossDaylightSavings
                ? 'check'
                : 'times'
            "
            class="h-4 w-4"
          />
        </p>
      </div>
    </div>
    <div class="text-sm">
      <p class="font-medium text-gray-700">
        {{ $t('instances.quartz.skip_day_if_hour_does_not_exist') }}
      </p>
      <p class="mt-1 flex items-center gap-2">
        <font-awesome-icon
          :icon="
            trigger.calendarInterval.skipDayIfHourDoesNotExist
              ? 'check'
              : 'times'
          "
          class="h-4 w-4"
        />
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
interface CalendarIntervalConfig {
  interval: number;
  timeZone?: string;
  timesTriggered: number;
  preserveHourOfDayAcrossDaylightSavings: boolean;
  skipDayIfHourDoesNotExist: boolean;
}

interface TriggerDetail {
  calendarInterval: CalendarIntervalConfig;
  [key: string]: unknown;
}

interface Props {
  trigger: TriggerDetail;
}

defineProps<Props>();

const formatInterval = (milliseconds: number): string => {
  const seconds = milliseconds / 1000;
  if (seconds < 60) return `${seconds} seconds`;
  const minutes = seconds / 60;
  if (minutes < 60) return `${minutes.toFixed(1)} minutes`;
  const hours = minutes / 60;
  if (hours < 24) return `${hours.toFixed(1)} hours`;
  const days = hours / 24;
  return `${days.toFixed(1)} days`;
};
</script>
