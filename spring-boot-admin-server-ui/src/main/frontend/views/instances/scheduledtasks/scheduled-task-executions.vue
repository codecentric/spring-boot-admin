<template>
  <td class="font-mono text-sm">
    <sba-formatted-obj
      v-if="task.nextExecution"
      :value="task.nextExecution.time"
    />
  </td>
  <td class="font-mono text-sm">
    <sba-formatted-obj
      v-if="task.lastExecution"
      :value="task.lastExecution.time"
    />
  </td>
  <td class="font-mono text-sm">
    <span
      v-if="task.lastExecution"
      role="status"
      :class="
        classNames('status-badge', task.lastExecution.status.toLowerCase())
      "
      v-text="task.lastExecution.status"
    />
  </td>
</template>

<script>
import classNames from 'classnames';

import SbaFormattedObj from '@/components/sba-formatted-obj.vue';

export default {
  name: 'ScheduledTaskExecutions',
  components: { SbaFormattedObj },
  props: {
    task: {
      type: Object,
      required: true,
    },
  },
  methods: {
    classNames,
  },
};
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
