<template>
  <sba-nav-dropdown data-testid="usermenu">
    <template #label>
      <font-awesome-icon
        class="w-10 rounded-full white mr-2"
        color="white"
        icon="user-circle"
      />
      <strong v-text="username" />
    </template>
    <sba-dropdown-item>
      Signed in as: <strong v-text="username" />
    </sba-dropdown-item>

    <template v-if="userSubMenuItems.length > 0">
      <sba-dropdown-divider />

      <template v-for="item in userSubMenuItems" :key="item.name">
        <sba-dropdown-item
          v-if="!item.href && item.name"
          :to="{ name: item.name }"
        >
          <component :is="item.handle" />
        </sba-dropdown-item>
        <sba-dropdown-item
          v-else-if="item.href !== '#'"
          :href="item.href"
          target="blank"
        >
          <component :is="item.handle" />
        </sba-dropdown-item>
        <sba-dropdown-item v-else>
          <component :is="item.handle" />
        </sba-dropdown-item>
      </template>
    </template>

    <sba-dropdown-divider />

    <sba-dropdown-logout-item />
  </sba-nav-dropdown>
</template>

<script lang="ts" setup>
import { computed } from 'vue';

import SbaDropdownDivider from '@/components/sba-dropdown/sba-dropdown-divider.vue';
import SbaDropdownItem from '@/components/sba-dropdown/sba-dropdown-item.vue';
import SbaNavDropdown from '@/components/sba-nav/sba-nav-dropdown.vue';

import { useViewRegistry } from '@/composables/ViewRegistry';
import { getCurrentUser } from '@/sba-config';
import SbaDropdownLogoutItem from '@/shell/sba-dropdown-logout-item.vue';

const currentUser = getCurrentUser();
const username = currentUser ? currentUser.name : null;

const { views } = useViewRegistry();

const userSubMenuItems = computed(() => {
  return views.filter((v) => v.parent === 'user');
});
</script>
