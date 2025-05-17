<template>
  <sba-panel>
    <template v-if="applicationsCount > 0">
      <div class="flex flex-row md:flex-col items-center justify-center">
        <template v-if="statusInfo.allUp">
          <font-awesome-icon icon="check-circle" class="text-green-500 icon" />
          <div class="text-center">
            <h1 class="font-bold text-2xl" v-text="$t('applications.all_up')" />
            <p class="text-gray-400" v-text="lastUpdate" />
          </div>
        </template>
        <template v-else-if="statusInfo.allDown">
          <font-awesome-icon icon="minus-circle" class="text-red-500 icon" />
          <div class="text-center">
            <h1
              class="font-bold text-2xl"
              v-text="$t('applications.all_down')"
            />
            <p class="text-gray-400" v-text="lastUpdate" />
          </div>
        </template>
        <template v-if="statusInfo.allUnknown">
          <font-awesome-icon
            icon="question-circle"
            class="text-gray-300 icon"
          />
          <div class="text-center">
            <h1
              class="font-bold text-2xl"
              v-text="$t('applications.all_unknown')"
            />
            <p class="text-gray-400" v-text="lastUpdate" />
          </div>
        </template>
        <template v-else-if="someInstancesDown">
          <font-awesome-icon icon="minus-circle" class="text-red-500 icon" />
          <div class="text-center">
            <h1
              class="font-bold text-2xl"
              v-text="$t('applications.some_down')"
            />
            <p class="text-gray-400" v-text="lastUpdate" />
          </div>
        </template>

        <template v-else-if="someInstancesUnknown">
          <font-awesome-icon
            icon="question-circle"
            class="text-gray-300 icon"
          />
          <div class="text-center">
            <h1
              class="font-bold text-2xl"
              v-text="$t('applications.some_unknown')"
            />
            <p class="text-gray-400" v-text="lastUpdate" />
          </div>
        </template>
      </div>
    </template>
    <template v-else>
      <div class="flex flex-col items-center">
        <font-awesome-icon
          icon="frown-open"
          class="text-gray-500 text-9xl pb-4"
        />
        <h1
          class="font-bold text-2xl"
          v-text="$t('applications.no_applications_registered')"
        />
      </div>
    </template>
  </sba-panel>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import { getStatusInfo } from '@/services/application';

const options = {
  year: 'numeric',
  month: 'numeric',
  day: 'numeric',
  hour: 'numeric',
  minute: 'numeric',
  second: 'numeric',
};
const { applications } = useApplicationStore();

const userLocale = navigator.languages
  ? navigator.languages[0]
  : navigator.language;
const dateTimeFormat = new Intl.DateTimeFormat(userLocale, options);

const lastUpdate = ref(dateTimeFormat.format(new Date()));

const statusInfo = computed(() => {
  return getStatusInfo(applications.value);
});

watch(statusInfo, () => {
  lastUpdate.value = dateTimeFormat.format(new Date());
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
.icon {
  @apply text-9xl pr-4 md:pb-4 md:pr-0;
}
</style>
