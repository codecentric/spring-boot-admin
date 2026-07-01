<template>
  <div class="grid grid-cols-2 gap-6 text-sm">
    <div>
      <p class="font-medium text-gray-700">
        {{ $t('instances.quartz.cron_expression') }}
      </p>
      <p class="mt-1 font-mono bg-gray-100 rounded p-2 text-gray-900 break-all">
        {{ trigger.cron.expression }}
      </p>
    </div>
    <div>
      <p class="font-medium text-gray-700">
        {{ $t('instances.quartz.time_zone') }}
      </p>
      <p class="mt-1 text-gray-600">
        {{ trigger.cron.timeZone || $t('instances.quartz.system_default') }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
interface CronConfig {
  expression: string;
  timeZone?: string;
}

interface TriggerDetail {
  cron: CronConfig;
  [key: string]: unknown;
}

interface Props {
  trigger: TriggerDetail;
}

defineProps<Props>();
</script>
