<template>
  <div class="space-y-6">
    <div class="grid grid-cols-4 gap-6 text-sm">
      <div>
        <p class="font-medium text-gray-700">
          {{ $t('instances.quartz.interval') }}
        </p>
        <p class="mt-1 text-gray-600">
          {{ formatInterval(trigger.dailyTimeInterval.interval) }}
        </p>
      </div>
      <div>
        <p class="font-medium text-gray-700">
          {{ $t('instances.quartz.days_of_week') }}
        </p>
        <p class="mt-1 text-gray-600">
          {{ formatDaysOfWeek(trigger.dailyTimeInterval.daysOfWeek) }}
        </p>
      </div>
      <div>
        <p class="font-medium text-gray-700">
          {{ $t('instances.quartz.start_time') }}
        </p>
        <p class="mt-1 text-gray-600">
          {{ trigger.dailyTimeInterval.startTimeOfDay || '—' }}
        </p>
      </div>
      <div>
        <p class="font-medium text-gray-700">
          {{ $t('instances.quartz.end_time') }}
        </p>
        <p class="mt-1 text-gray-600">
          {{ trigger.dailyTimeInterval.endTimeOfDay || '—' }}
        </p>
      </div>
    </div>
    <div class="grid grid-cols-2 gap-6 text-sm">
      <div>
        <p class="font-medium text-gray-700">
          {{ $t('instances.quartz.repeat_count') }}
        </p>
        <p class="mt-1 text-gray-600">
          {{
            trigger.dailyTimeInterval.repeatCount === -1
              ? $t('instances.quartz.infinite')
              : trigger.dailyTimeInterval.repeatCount
          }}
        </p>
      </div>
      <div>
        <p class="font-medium text-gray-700">
          {{ $t('instances.quartz.times_triggered') }}
        </p>
        <p class="mt-1 text-gray-600">
          {{ trigger.dailyTimeInterval.timesTriggered }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

interface DailyTimeIntervalConfig {
  interval: number;
  daysOfWeek: number[];
  startTimeOfDay?: string;
  endTimeOfDay?: string;
  repeatCount: number;
  timesTriggered: number;
}

interface TriggerDetail {
  dailyTimeInterval: DailyTimeIntervalConfig;
  [key: string]: unknown;
}

interface Props {
  trigger: TriggerDetail;
}

defineProps<Props>();

const { t } = useI18n();

const formatInterval = (milliseconds: number): string => {
  const seconds = milliseconds / 1000;
  if (seconds < 60) return `${seconds} seconds`;
  const minutes = seconds / 60;
  if (minutes < 60) return `${minutes.toFixed(1)} minutes`;
  const hours = minutes / 60;
  return `${hours.toFixed(1)} hours`;
};

const formatDaysOfWeek = (days: number[]): string => {
  const dayNames: Record<number, string> = {
    1: t('instances.quartz.sunday'),
    2: t('instances.quartz.monday'),
    3: t('instances.quartz.tuesday'),
    4: t('instances.quartz.wednesday'),
    5: t('instances.quartz.thursday'),
    6: t('instances.quartz.friday'),
    7: t('instances.quartz.saturday'),
  };
  return days.map((d) => dayNames[d] || d).join(', ');
};
</script>
