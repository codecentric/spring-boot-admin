<template>
  <div class="link-wrapper">
    <div class="link" v-text="userName"></div>
    <NavbarMenu>
      <template #menuItems>
        <div v-if="userName" class="link link--no-hover">
          <span>
            {{ $t('navbar.signedInAs') }} <strong v-text="userName" />
          </span>
        </div>
        <hr class="pb-1" />
        <NavbarLink
          v-for="userSubMenuItem in submenuItems"
          :key="userSubMenuItem.name"
          :view="userSubMenuItem"
        />

        <form action="logout" method="post">
          <input
            v-if="csrfToken"
            type="hidden"
            :name="csrfParameterName"
            :value="csrfToken"
          />
          <button type="submit" value="logout" class="link">
            <font-awesome-icon icon="sign-out-alt" />&nbsp;<span
              v-text="$t('navbar.logout')"
            />
          </button>
        </form>
      </template>
    </NavbarMenu>
  </div>
</template>

<script>
import NavbarLink from '@/shell/NavbarLink';
import NavbarMenu from '@/shell/NavbarMenu';

export default {
  name: 'NavbarUserMenu',
  components: { NavbarLink, NavbarMenu },
  props: {
    csrfParameterName: {
      type: String,
      default: undefined,
    },
    userName: {
      type: String,
      default: undefined,
    },
    csrfToken: {
      type: String,
      default: undefined,
    },
    submenuItems: {
      type: Array,
      default: () => [],
    },
  },
};
</script>

<style scoped>
.link {
  @apply flex w-full items-center rounded-md px-3 pt-2 break-all;
}

.link--no-hover {
  @apply hover:text-inherit hover:bg-inherit;
}
</style>
