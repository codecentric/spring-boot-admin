<template>
  <sba-nav-dropdown>
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

      <sba-dropdown-item
        v-for="child in userSubMenuItems"
        :key="child.name"
        v-bind="{ ...child }"
      >
        <component :is="child.handle" />
      </sba-dropdown-item>
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
import sbaConfig from '@/sba-config';
import SbaDropdownLogoutItem from '@/shell/sba-dropdown-logout-item.vue';

const username = sbaConfig.user ? sbaConfig.user.name : null;
const { views } = useViewRegistry();

const userSubMenuItems = computed(() => {
  return views.filter((v) => v.parent === 'user');
});
</script>
