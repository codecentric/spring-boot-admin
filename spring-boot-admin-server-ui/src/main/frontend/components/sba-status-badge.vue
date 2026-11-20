<template>
  <span
    role="status"
    :class="classNames('status-badge', healthStatus)"
    v-text="status"
  />
</template>

<script>
import classNames from 'classnames';

import { HealthStatus } from '@/HealthStatus';

export default {
  props: {
    status: {
      type: [HealthStatus, String, Number],
      required: true,
    },
  },
  computed: {
    healthStatus() {
      return `${this.status}`.toLowerCase();
    },
  },
  methods: {
    classNames,
  },
};
</script>

<style scoped>
@reference "../index.css";

.status-badge {
  @apply bg-gray-200 text-black text-xs inline-flex items-center uppercase  rounded overflow-hidden px-3 py-1;
}
.up {
  @apply bg-status-up-bg text-status-up-text;
}
.down,
.out_of_service {
  @apply bg-status-down-bg text-status-down-text;
}
.restricted {
  @apply bg-status-restricted-bg text-status-restricted-text;
}
</style>
