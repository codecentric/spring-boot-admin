<template>
  <div class="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
    <quartz-summary-card
      :label="$t('instances.quartz.jobs')"
      :value="jobCount"
      icon="briefcase"
    />
    <quartz-summary-card
      :label="$t('instances.quartz.triggers')"
      :value="triggerCount"
      icon="clock"
    />
    <quartz-summary-card
      :label="$t('instances.quartz.active_triggers')"
      :value="activeTriggerCount"
      icon="play-circle"
    />
    <quartz-summary-card
      :label="$t('instances.quartz.paused_triggers')"
      :value="pausedTriggerCount"
      icon="pause-circle"
    />
  </div>
</template>

<script setup lang="ts">
import QuartzSummaryCard from './quartz-summary-card.vue';

defineProps<{
  jobCount: number;
  triggerCount: number;
  activeTriggerCount: number;
  pausedTriggerCount: number;
}>();
</script>
