<template>
  <Menu as="div" class="submenu" v-bind="$attrs">
    <div
      :class="{
        'submenu-opener--link': href,
        'submenu-opener--active': active,
      }"
      class="submenu-opener"
    >
      <a v-if="href" :href="href" class="submenu-opener-label" target="_blank">
        <span v-if="text" v-text="text" />
        <slot v-else name="label" />
      </a>

      <MenuButton class="submenu-opener-button">
        <span v-if="!href" class="submenu-opener-label">
          <span v-if="text" v-text="text" />
          <slot v-else-if="!text" name="label" />
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
  href: {
    type: String,
    default: null,
  },
  active: {
    type: Boolean,
    default: false,
  },
});
</script>

<style scoped>
.submenu {
  @apply inline-block text-left text-white cursor-pointer lg:relative;
}

.submenu-opener {
  @apply flex items-center;
}

.submenu-opener > button {
  @apply flex items-center px-3 py-2 rounded hover:bg-sba-700;
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
  @apply hover:bg-sba-700 rounded;
}
.submenu-opener--link > a {
  @apply px-3 py-2;
}

.submenu-opener--link .submenu-opener-button {
  @apply px-3 py-3 hover:bg-sba-800 rounded-r rounded-l-none border-l border-black ml-auto w-auto;
}
</style>
