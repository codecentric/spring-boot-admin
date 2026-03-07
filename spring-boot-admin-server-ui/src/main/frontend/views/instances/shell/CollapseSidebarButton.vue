<script setup lang="ts">
import { faSquareCaretLeft } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

defineEmits<{
  (e: 'toggle-sidebar'): void;
}>();
withDefaults(
  defineProps<{
    open?: boolean;
  }>(),
  {
    open: false,
  },
);
</script>

<template>
  <button @click="() => $emit('toggle-sidebar')">
    <font-awesome-icon
      :icon="faSquareCaretLeft"
      :class="{
        'rotate-180': !open,
      }"
    />
  </button>
</template>
