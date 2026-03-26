<template>
  <span :class="badgeClass">
    <font-awesome-icon :icon="stateIcon" class="mr-1 h-3 w-3" />
    {{ state }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';

type StateType =
  | 'NORMAL'
  | 'PAUSED'
  | 'COMPLETE'
  | 'ERROR'
  | 'BLOCKED'
  | 'NONE'
  | string;

interface Props {
  state?: StateType;
}

const props = withDefaults(defineProps<Props>(), {
  state: 'UNKNOWN' as StateType,
});

const stateColorMap: Record<StateType, string> = {
  NORMAL: 'bg-green-100 text-green-800',
  PAUSED: 'bg-yellow-100 text-yellow-800',
  COMPLETE: 'bg-blue-100 text-blue-800',
  ERROR: 'bg-red-100 text-red-800',
  BLOCKED: 'bg-red-100 text-red-800',
  NONE: 'bg-gray-100 text-gray-800',
};

const stateIconMap: Record<StateType, string> = {
  NORMAL: 'play-circle',
  PAUSED: 'pause-circle',
  COMPLETE: 'check-circle',
  ERROR: 'exclamation-circle',
  BLOCKED: 'ban',
  NONE: 'question-circle',
};

const badgeClass = computed(() => {
  const baseClass =
    'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium';
  const colorClass =
    stateColorMap[props.state as StateType] || stateColorMap.NONE;
  return `${baseClass} ${colorClass}`;
});

const stateIcon = computed(
  () => stateIconMap[props.state as StateType] || stateIconMap.NONE,
);
</script>
