<template>
  <div v-if="error" class="mb-6 rounded bg-gray-50 p-4 border border-gray-200">
    <div class="flex gap-3">
      <font-awesome-icon class="mt-0.5 h-5 w-5" icon="exclamation-triangle" />
      <div>
        <h3 class="font-semibold text-gray-900">
          {{ $t('term.fetch_failed') }}
        </h3>
        <p class="mt-1 text-sm text-gray-700">{{ error.message }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  error?: Error | null;
}

withDefaults(defineProps<Props>(), {
  error: null,
});
</script>
