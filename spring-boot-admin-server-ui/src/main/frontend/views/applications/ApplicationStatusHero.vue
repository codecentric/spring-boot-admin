<template>
  <sba-panel>
    <div class="flex flex-row items-center justify-center my-2">
      <template v-if="applicationsCount > 0">
        <application-status-overview
          v-if="statusInfo.allUp"
          icon-name="check-circle"
          icon-color="text-green-500"
          :last-update="lastUpdate"
          status-label-text-key="applications.all_up"
        />
        <application-status-overview
          v-else-if="statusInfo.allDown"
          icon-name="minus-circle"
          icon-color="text-red-500"
          :last-update="lastUpdate"
          status-label-text-key="applications.all_down"
        />
        <application-status-overview
          v-if="statusInfo.allUnknown"
          icon-name="question-circle"
          icon-color="text-gray-300"
          :last-update="lastUpdate"
          status-label-text-key="applications.all_unknown"
        />
        <application-status-overview
          v-else-if="someInstancesDown"
          icon-name="minus-circle"
          icon-color="text-red-500"
          :last-update="lastUpdate"
          status-label-text-key="applications.some_down"
        />
        <application-status-overview
          v-else-if="someInstancesUnknown"
          icon-name="question-circle"
          icon-color="text-gray-300"
          :last-update="lastUpdate"
          status-label-text-key="applications.some_unknown"
        />
      </template>
      <template v-else>
        <application-status-overview
          icon-name="frown-open"
          icon-color="text-gray-500"
          status-label-text-key="applications.no_applications_registered"
        />
      </template>
    </div>
  </sba-panel>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import { getStatusInfo } from '@/services/application';
import ApplicationStatusOverview from '@/views/applications/ApplicationStatusOverview.vue';

const { applications } = useApplicationStore();

const lastUpdate = ref(new Date());

const statusInfo = computed(() => {
  return getStatusInfo(applications.value);
});

watch(statusInfo, () => {
  lastUpdate.value = new Date();
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
