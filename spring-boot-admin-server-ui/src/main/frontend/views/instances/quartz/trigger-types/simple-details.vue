<template>
  <div class="grid grid-cols-3 gap-6 text-sm">
    <div>
      <p class="font-medium text-gray-700">
        {{ $t('instances.quartz.interval') }}
      </p>
      <p class="mt-1 text-gray-600">
        {{ formatInterval(trigger.simple.interval) }}
      </p>
    </div>
    <div>
      <p class="font-medium text-gray-700">
        {{ $t('instances.quartz.repeat_count') }}
      </p>
      <p class="mt-1 text-gray-600">
        {{
          trigger.simple.repeatCount === -1
            ? $t('instances.quartz.infinite')
            : trigger.simple.repeatCount
        }}
      </p>
    </div>
    <div>
      <p class="font-medium text-gray-700">
        {{ $t('instances.quartz.times_triggered') }}
      </p>
      <p class="mt-1 text-gray-600">{{ trigger.simple.timesTriggered }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
interface SimpleConfig {
  interval: number;
  repeatCount: number;
  timesTriggered: number;
}

interface TriggerDetail {
  simple: SimpleConfig;
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
  return `${hours.toFixed(1)} hours`;
};
</script>
