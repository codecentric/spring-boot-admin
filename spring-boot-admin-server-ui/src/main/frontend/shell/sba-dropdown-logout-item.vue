<template>
  <form action="logout" class="w-full" method="post">
    <sba-dropdown-item as="button" type="submit">
      <input
        v-if="csrfToken"
        :name="csrfParameterName"
        :value="csrfToken"
        type="hidden"
      />
      <font-awesome-icon icon="sign-out-alt" />&nbsp;<span
        v-text="$t('navbar.logout')"
      />
    </sba-dropdown-item>
  </form>
</template>

<script lang="ts" setup>
import SbaDropdownItem from '@/components/sba-dropdown/sba-dropdown-item.vue';

import sbaConfig from '@/sba-config';

const readCookie = (name) => {
  const match = document.cookie.match(
    new RegExp('(^|;\\s*)(' + name + ')=([^;]*)'),
  );
  return match ? decodeURIComponent(match[3]) : null;
};

const csrfToken = readCookie('XSRF-TOKEN');
const csrfParameterName = sbaConfig.csrf.parameterName;
</script>
