<template>
  <td class="font-mono text-sm">
    <sba-formatted-obj
      v-if="task.nextExecution"
      :value="formatDateTime(task.nextExecution.time)"
    />
  </td>
  <td class="font-mono text-sm">
    <sba-formatted-obj
      v-if="task.lastExecution"
      data-testid="lastExecution"
      :value="formatDateTime(task.lastExecution.time)"
    />
  </td>
  <td class="font-mono text-sm">
    <span
      v-if="task.lastExecution"
      role="status"
      :class="`status-badge ${task.lastExecution.status.toLowerCase()}`"
      v-text="task.lastExecution.status"
    />
  </td>
</template>

<script setup lang="ts">
import SbaFormattedObj from '@/components/sba-formatted-obj.vue';

import { usePrettyTime } from '@/utils/prettyTime';

defineProps({
  task: {
    type: Object,
    required: true,
  },
});

const { formatDateTime } = usePrettyTime();
</script>

<style scoped>
.status-badge {
  @apply bg-gray-200 text-black text-xs inline-flex items-center uppercase  rounded overflow-hidden px-3 py-1;
}
.success {
  @apply bg-green-200 text-green-700;
}
.error {
  @apply bg-red-200 text-red-700;
}
.started {
  @apply bg-yellow-200 text-yellow-700;
}
</style>
