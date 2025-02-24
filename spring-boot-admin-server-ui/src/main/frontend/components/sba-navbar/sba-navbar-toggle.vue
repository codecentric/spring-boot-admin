<template>
  <button
    :aria-expanded="isMenuShown"
    aria-controls="mobile-menu"
    class="sba-navbar-toggle"
    type="button"
    @click="handleClick"
  >
    <span class="sr-only">Open main menu</span>
    <svg
      :class="{ block: !isMenuShown, hidden: isMenuShown }"
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
      :class="{ block: isMenuShown, hidden: !isMenuShown }"
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
import { ref } from 'vue';

const props = defineProps({
  showMenu: {
    type: Boolean,
    default: () => false,
  },
});

const emit = defineEmits(['click']);

const isMenuShown = ref(props.showMenu);

const handleClick = () => {
  isMenuShown.value = !isMenuShown.value;
  emit('click', isMenuShown.value);
};
</script>

<style>
.sba-navbar-toggle {
  @apply ml-auto inline-flex bg-gray-800 items-center justify-center p-2 rounded-md text-gray-400;
  @apply xl:hidden;
  @apply hover:text-white hover:bg-gray-700;
  @apply focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-800 focus:ring-white;
}
</style>
