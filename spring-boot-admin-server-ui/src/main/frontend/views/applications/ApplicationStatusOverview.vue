<template>
  <font-awesome-icon :icon="iconName" :class="[iconColor, 'icon']" />
  <div v-if="lastUpdate" class="text-center">
    <h1 class="status-label" v-text="$t(statusLabelTextKey)" />
    <p class="text-gray-400">
      {{ $t('applications.last_update') }}: {{ formattedLastUpdate }}
    </p>
  </div>
  <h1 v-else class="status-label" v-text="$t(statusLabelTextKey)" />
</template>

<script setup lang="ts">
import { computed } from 'vue';

import { useDateTimeFormatter } from '@/composables/useDateTimeFormatter';

const { formatDateTime } = useDateTimeFormatter();

const props = defineProps<{
  lastUpdate?: Date;
  iconName: string;
  iconColor: string;
  statusLabelTextKey: string;
}>();

const formattedLastUpdate = computed(() =>
  props.lastUpdate ? formatDateTime(props.lastUpdate) : '',
);
</script>

<style scoped>
@reference "../../index.css";
.status-label {
  @apply font-bold text-2xl;
}
.icon {
  @apply text-6xl pr-4;
}
</style>
