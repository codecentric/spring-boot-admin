<template>
  <section :class="{ 'opacity-50': !state.hasLoaded }" class="p-6">
    <template v-if="state.hasLoaded">
      <!-- Error State -->
      <error-alert :error="state.error" />

      <!-- No Data State -->
      <no-data-alert :show="!hasData && !state.error" />

      <!-- Main Content -->
      <template v-if="hasData">
        <!-- Summary Cards -->
        <quartz-summary
          v-if="!state.error"
          :job-count="state.jobDetails.length"
          :trigger-count="state.triggerDetails.length"
          :active-trigger-count="activeTriggerCount"
          :paused-trigger-count="pausedTriggerCount"
        />

        <!-- Jobs Section -->
        <jobs-section :jobs="state.jobDetails" :instance="props.instance" />

        <!-- Triggers Section -->
        <triggers-section :triggers="state.triggerDetails" />
      </template>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue';

import ErrorAlert from './error-alert.vue';
import JobsSection from './jobs-section.vue';
import NoDataAlert from './no-data-alert.vue';
import QuartzSummary from './quartz-summary.vue';
import TriggersSection from './triggers-section.vue';

import Instance from '@/services/instance';
import {
  type JobDetail,
  QuartzActuatorService,
  type TriggerDetail,
} from '@/services/quartz-actuator';
import '@/views/ViewGroup';

interface ComponentState {
  jobDetails: JobDetail[];
  triggerDetails: TriggerDetail[];
  error: Error | null;
  hasLoaded: boolean;
}

interface Props {
  instance: Instance;
}

const props = defineProps<Props>();

const state = reactive<ComponentState>({
  jobDetails: [],
  triggerDetails: [],
  error: null,
  hasLoaded: false,
});

const hasData = computed(
  () => state.jobDetails.length > 0 || state.triggerDetails.length > 0,
);
const activeTriggerCount = computed(
  () =>
    state.triggerDetails.filter((t) => t.state && t.state !== 'PAUSED').length,
);
const pausedTriggerCount = computed(
  () => state.triggerDetails.filter((t) => t.state === 'PAUSED').length,
);

onMounted(async () => {
  const [jobs, triggers] = await Promise.allSettled([
    QuartzActuatorService.fetchAllJobs(props.instance),
    QuartzActuatorService.fetchAllTriggers(props.instance),
  ]);
  if (jobs.status === 'fulfilled') state.jobDetails = jobs.value;
  if (triggers.status === 'fulfilled') state.triggerDetails = triggers.value;

  const failure = [jobs, triggers].find(
    (result) => result.status === 'rejected',
  );
  if (failure) {
    state.error =
      failure.reason instanceof Error
        ? failure.reason
        : new Error(String(failure.reason));
  }
  state.hasLoaded = true;
});
</script>

<script lang="ts">
import { VIEW_GROUP } from '@/views/ViewGroup';

export default {
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/quartz',
      parent: 'instances',
      path: 'quartz',
      label: 'instances.quartz.label',
      group: VIEW_GROUP.INSIGHTS,
      order: 50,
      component: this,
      isEnabled: ({ instance }: { instance: Instance }) =>
        instance.hasEndpoint('quartz'),
    });
  },
};
</script>
