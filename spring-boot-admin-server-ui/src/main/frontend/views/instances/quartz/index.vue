<template>
  <section :class="{ 'opacity-50': !state.hasLoaded }" class="p-6">
    <template v-if="state.hasLoaded">
      <!-- Error State -->
      <error-alert :error="state.error" />

      <!-- No Data State -->
      <no-data-alert
        :show="!computed.hasJobs && !computed.hasTriggers && !state.error"
      />

      <!-- Main Content -->
      <template
        v-if="!state.error && (computed.hasJobs || computed.hasTriggers)"
      >
        <!-- Summary Cards -->
        <quartz-summary
          :job-count="state.jobDetails.length"
          :trigger-count="state.triggerDetails.length"
          :active-trigger-count="computed.activeTriggerCount"
          :paused-trigger-count="computed.pausedTriggerCount"
        />

        <!-- Jobs Section -->
        <jobs-section :jobs="state.jobDetails" />

        <!-- Triggers Section -->
        <triggers-section :triggers="state.triggerDetails" />
      </template>
    </template>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, computed as vueComputed } from 'vue';

import ErrorAlert from './error-alert.vue';
import JobsSection from './jobs-section.vue';
import NoDataAlert from './no-data-alert.vue';
import QuartzSummary from './quartz-summary.vue';
import TriggersSection from './triggers-section.vue';

import Instance from '@/services/instance';
import { QuartzActuatorService } from '@/services/quartz-actuator';
import '@/views/ViewGroup';

interface JobDetail {
  group: string;
  name: string;
  [key: string]: unknown;
}

interface TriggerDetail {
  group: string;
  name: string;
  state: string;
  [key: string]: unknown;
}

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

const computed = {
  hasJobs: vueComputed(() => state.jobDetails && state.jobDetails.length > 0),
  hasTriggers: vueComputed(
    () => state.triggerDetails && state.triggerDetails.length > 0,
  ),
  activeTriggerCount: vueComputed(
    () =>
      state.triggerDetails.filter((t) => t.state && t.state !== 'PAUSED')
        .length,
  ),
  pausedTriggerCount: vueComputed(
    () => state.triggerDetails.filter((t) => t.state === 'PAUSED').length,
  ),
};

const loadData = async (instance: Instance): Promise<void> => {
  state.hasLoaded = false;
  state.error = null;

  try {
    const [jobsResults, triggersResults] = await Promise.all([
      Promise.allSettled([QuartzActuatorService.fetchAllJobs(instance)]),
      Promise.allSettled([QuartzActuatorService.fetchAllTriggers(instance)]),
    ]);

    state.jobDetails = jobsResults
      .filter((r) => r.status === 'fulfilled')
      .flatMap((r) => (r as PromiseFulfilledResult<JobDetail>).value);

    state.triggerDetails = triggersResults
      .filter((r) => r.status === 'fulfilled')
      .flatMap((r) => (r as PromiseFulfilledResult<TriggerDetail>).value);
  } catch (error) {
    console.warn('Fetching Quartz data failed:', error);
    state.error = error instanceof Error ? error : new Error(String(error));
  } finally {
    state.hasLoaded = true;
  }
};

onMounted(() => {
  loadData(props.instance);
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
