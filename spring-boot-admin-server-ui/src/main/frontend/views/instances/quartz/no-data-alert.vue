<template>
  <div v-if="show" class="mb-6 rounded bg-amber-50 p-4 border border-amber-200">
    <div class="flex gap-3">
      <font-awesome-icon class="mt-0.5 h-5 w-5" icon="info-circle" />
      <p class="text-amber-800">{{ $t('instances.quartz.no_data') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  show: boolean;
}

defineProps<Props>();
</script>
