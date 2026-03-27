<template>
  <div class="text-sm">
    <p class="font-medium text-gray-700">
      {{ $t('instances.quartz.custom_trigger') }}
    </p>
    <p class="mt-1 font-mono bg-gray-100 rounded p-2 text-gray-900 break-all">
      {{ trigger.custom.trigger }}
    </p>
  </div>
</template>

<script setup lang="ts">
interface CustomConfig {
  trigger: string;
}

interface TriggerDetail {
  custom: CustomConfig;
  [key: string]: unknown;
}

interface Props {
  trigger: TriggerDetail;
}

defineProps<Props>();
</script>
