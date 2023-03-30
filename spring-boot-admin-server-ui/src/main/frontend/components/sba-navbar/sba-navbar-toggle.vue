<template>
  <button
    :aria-expanded="showMenu"
    aria-controls="mobile-menu"
    class="ml-auto lg:hidden bg-gray-800 inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-white hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-800 focus:ring-white"
    type="button"
    @click="() => emit('click')"
  >
    <span class="sr-only">Open main menu</span>
    <svg
      :class="{ block: !showMenu, hidden: showMenu }"
      aria-hidden="true"
      class="h-6 w-6"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        d="M4 6h16M4 12h16M4 18h16"
        stroke-linecap="round"
        stroke-linejoin="round"
        stroke-width="2"
      />
    </svg>
    <svg
      :class="{ block: showMenu, hidden: !showMenu }"
      aria-hidden="true"
      class="h-6 w-6"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        d="M6 18L18 6M6 6l12 12"
        stroke-linecap="round"
        stroke-linejoin="round"
        stroke-width="2"
      />
    </svg>
  </button>
</template>

<script lang="ts" setup>
defineProps({
  showMenu: {
    type: Boolean,
    default: () => false,
  },
});

const emit = defineEmits(['click']);
</script>
