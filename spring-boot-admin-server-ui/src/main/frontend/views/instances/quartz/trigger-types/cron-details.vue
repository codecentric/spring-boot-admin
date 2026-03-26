<template>
  <div class="grid grid-cols-2 gap-6 text-sm">
    <div>
      <p class="font-medium text-gray-700">Cron Expression</p>
      <p class="mt-1 font-mono bg-gray-100 rounded p-2 text-gray-900 break-all">
        {{ trigger.cron.expression }}
      </p>
    </div>
    <div>
      <p class="font-medium text-gray-700">Time Zone</p>
      <p class="mt-1 text-gray-600">
        {{ trigger.cron.timeZone || 'System Default' }}
      </p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CronTriggerDetails',
  props: {
    trigger: {
      type: Object,
      required: true,
    },
  },
};
</script>
