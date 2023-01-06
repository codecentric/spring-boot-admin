<!--
  - Copyright 2014-2018 the original author or authors.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <Menu as="div" class="submenu">
    <MenuButton
      class="submenu-opener-button"
      :aria-label="$t('term.menu.open')"
    >
      <font-awesome-icon
        class="submenu-opener-icon"
        :icon="['fas', 'chevron-down']"
      />
    </MenuButton>

    <transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0 translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-1"
    >
      <MenuItems class="submenu-items">
        <div class="px-1 py-1">
          <slot name="menuItems">
            <MenuItem
              v-for="menuItem in menuItems"
              :key="menuItem.label"
              @click="$emit('menuItemClicked', menuItem)"
            >
              <NavbarLink :view="menuItem" />
            </MenuItem>
          </slot>
        </div>
      </MenuItems>
    </transition>
  </Menu>
</template>

<script>
import { Menu, MenuButton, MenuItem, MenuItems } from '@headlessui/vue';
import { defineAsyncComponent } from 'vue';

export default {
  components: {
    NavbarLink: defineAsyncComponent(() => import('@/shell/NavbarLink')),
    Menu,
    MenuButton,
    MenuItem,
    MenuItems,
  },
  props: {
    buttonType: { type: String, default: 'button' },
    menuItems: { type: Array, default: () => [] },
  },
  emits: ['menuItemClicked'],
};
</script>

<style scoped>
.submenu {
  @apply inline-block text-left;
}

.submenu-opener-button {
  @apply pl-2 border-l border-black;
}

.submenu-items {
  @apply absolute right-0 text-black w-56 origin-top-right divide-y divide-gray-100 rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none;
}
</style>
