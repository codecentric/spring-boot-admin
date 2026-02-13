<template>
  <sba-panel>
    <div class="flex flex-row items-center justify-center my-2">
      <template v-if="applicationsCount > 0">
        <template v-if="statusInfo.allUp">
          <font-awesome-icon icon="check-circle" class="text-green-500 icon" />
          <div class="text-center">
            <h1 class="status-label" v-text="$t('applications.all_up')" />
            <p class="text-gray-400" v-text="lastUpdate" />
          </div>
        </template>
        <template v-else-if="statusInfo.allDown">
          <font-awesome-icon icon="minus-circle" class="text-red-500 icon" />
          <div class="text-center">
            <h1 class="status-label" v-text="$t('applications.all_down')" />
            <p class="text-gray-400" v-text="lastUpdate" />
          </div>
        </template>
        <template v-if="statusInfo.allUnknown">
          <font-awesome-icon
            icon="question-circle"
            class="text-gray-300 icon"
          />
          <div class="text-center">
            <h1 class="status-label" v-text="$t('applications.all_unknown')" />
            <p class="text-gray-400" v-text="lastUpdate" />
          </div>
        </template>
        <template v-else-if="someInstancesDown">
          <font-awesome-icon icon="minus-circle" class="text-red-500 icon" />
          <div class="text-center">
            <h1 class="status-label" v-text="$t('applications.some_down')" />
            <p class="text-gray-400" v-text="lastUpdate" />
          </div>
        </template>

        <template v-else-if="someInstancesUnknown">
          <font-awesome-icon
            icon="question-circle"
            class="text-gray-300 icon"
          />
          <div class="text-center">
            <h1 class="status-label" v-text="$t('applications.some_unknown')" />
            <p class="text-gray-400" v-text="lastUpdate" />
          </div>
        </template>
      </template>
      <template v-else>
        <font-awesome-icon icon="frown-open" class="text-gray-500 icon" />
        <h1
          class="status-label"
          v-text="$t('applications.no_applications_registered')"
        />
      </template>
    </div>
  </sba-panel>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import { useDateTimeFormatter } from '@/composables/useDateTimeFormatter';
import { getStatusInfo } from '@/services/application';

const { applications } = useApplicationStore();
const { formatDateTime } = useDateTimeFormatter();

const lastUpdate = ref(formatDateTime(new Date()));

const statusInfo = computed(() => {
  return getStatusInfo(applications.value);
});

watch(statusInfo, () => {
  lastUpdate.value = formatDateTime(new Date());
});

const applicationsCount = computed(() => {
  return applications.value.length;
});

const someInstancesDown = computed(() => {
  return statusInfo.value.someDown;
});

const someInstancesUnknown = computed(() => {
  return statusInfo.value.someUnknown;
});
</script>

<style scoped>
.status-label {
  @apply font-bold text-2xl;
}
.icon {
  @apply text-6xl pr-4;
}
</style>
