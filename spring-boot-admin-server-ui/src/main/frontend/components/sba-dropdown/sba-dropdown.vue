<template>
  <Menu as="div" class="submenu">
    <div class="submenu-opener">
      <MenuButton class="submenu-opener-button">
        <span class="submenu-opener-label">
          <template v-if="text">
            <span v-text="text" />
          </template>
          <component :is="is" v-else />
        </span>
        <font-awesome-icon
          :icon="['fas', 'chevron-down']"
          class="submenu-opener-icon"
        />
      </MenuButton>
    </div>

    <transition
      enter-active-class="transition duration-100 ease-out"
      enter-from-class="transform scale-95 opacity-0"
      enter-to-class="transform scale-100 opacity-100"
      leave-active-class="transition duration-75 ease-in"
      leave-from-class="transform scale-100 opacity-100"
      leave-to-class="transform scale-95 opacity-0"
    >
      <MenuItems class="submenu-items">
        <div class="px-1 py-1">
          <slot />
        </div>
      </MenuItems>
    </transition>
  </Menu>
</template>

<script setup>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { Menu, MenuButton, MenuItems } from '@headlessui/vue';

defineProps({
  text: {
    type: String,
    default: null,
  },
  is: {
    type: Object,
    default: null,
  },
});
</script>

<style scoped>
.submenu {
  @apply inline-block text-left text-white cursor-pointer lg:relative;
}

.submenu-opener {
  @apply flex items-center px-3 py-2 rounded bg-sba-700;
}

.submenu-opener--active {
  @apply bg-sba-700;
}

.submenu-opener-label {
  @apply pr-3 items-center truncate;
}

.submenu-opener-icon {
  @apply ml-auto;
}

.submenu-opener-button {
  @apply flex flex-row items-center w-full;
}

.submenu-items {
  @apply absolute right-0 text-black w-56 origin-top-right divide-y divide-gray-100 rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none;
}

.submenu-opener--link {
  @apply py-0 pr-0;
}

.submenu-opener--link .submenu-opener-button {
  @apply px-3 py-3 hover:bg-sba-800 rounded-r border-l border-black ml-auto w-auto;
}
</style>
