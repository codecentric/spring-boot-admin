<template>
  <MenuItem
    :active="active"
    :as="as"
    :disabled="disabled"
    class="block w-full"
    type="submit"
    v-bind="$attrs"
    @click="onClick"
  >
    <a
      v-if="href"
      :aria-disabled="disabled"
      :href="href"
      class="sba-dropdown-item"
      rel="noopener noreferrer"
      target="_blank"
    >
      <slot :active="active" />
    </a>

    <router-link
      v-else-if="to"
      :aria-disabled="disabled"
      :to="to"
      class="sba-dropdown-item"
    >
      <slot :active="active" />
    </router-link>

    <div v-else class="sba-dropdown-item">
      <slot />
    </div>
  </MenuItem>
</template>

<script setup>
import { MenuItem } from '@headlessui/vue';

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false,
  },
  active: {
    type: Boolean,
    default: false,
  },
  as: {
    type: String,
    default: 'div',
  },
  href: {
    type: String,
    default: null,
  },
  to: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['click']);

const onClick = ($event) => {
  if (!props.href && !props.to) {
    emit('click', $event);
  }
};
</script>

<style scoped>
.sba-dropdown-item {
  @apply flex w-full items-center rounded-md px-2 py-2 hover:bg-sba-700 hover:text-white;
}

.sba-dropdown-item[aria-disabled='true'] {
  @apply text-gray-400 hover:text-gray-400 hover:bg-transparent cursor-not-allowed;
}

.sba-dropdown-item[active='true'] {
  @apply bg-sba-700 text-white;
}
</style>
