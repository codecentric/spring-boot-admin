<template>
  <MenuItem v-slot="{ active }" :active="active" :disabled="disabled">
    <div :active="active" :aria-disabled="disabled" class="sba-dropdown-item">
      <slot :active="active" />
    </div>
  </MenuItem>
</template>

<script setup>
import { MenuItem } from '@headlessui/vue';

defineProps({
  disabled: {
    type: Boolean,
    default: false,
  },
  active: {
    type: Boolean,
    default: false,
  },
});
</script>

<style scoped>
.sba-dropdown-item {
  @apply flex w-full items-center rounded-md px-2 py-2 text-sm hover:bg-sba-700 hover:text-white;
}
.sba-dropdown-item[aria-disabled='true'] {
  @apply text-gray-400 hover:text-gray-400 hover:bg-transparent cursor-not-allowed;
}
.sba-dropdown-item[active='true'] {
  @apply bg-sba-700 text-white;
}
</style>
