<template>
  <NavbarMenu :menu-items="['']">
    <template #default>
      <div class="px-0.5 py-0.5">
        <span
          class="sr-only"
        >
          Open user menu</span>
        <font-awesome-icon
          color="white"
          class="rounded-full"
          icon="user-circle"
          size="2x"
        />
      </div>
    </template>

    <template #menuItems>

      <NavbarLink v-for="userSubMenuItem in submenuItems" :key="userSubMenuItem.name" :view="userSubMenuItem"/>

      <form
        action="logout"
        method="post"
      >
        <input
          v-if="csrfToken"
          type="hidden"
          :name="csrfParameterName"
          :value="csrfToken"
        >
        <button
          type="submit"
          value="logout"
          class="link"
        >
          <font-awesome-icon icon="sign-out-alt" />&nbsp;<span v-text="$t('navbar.logout')" />
        </button>
      </form>
    </template>
  </NavbarMenu>
</template>

<script>
import NavbarMenu from "./navbar-menu.vue";
import NavbarLink from "./NavbarLink.vue";

export default {
  name: 'NavbarUserMenu',
  components: {NavbarMenu, NavbarLink},
  props: {
    csrfParameterName: {
      type: String,
      default: undefined
    },
    csrfToken: {
      type: String,
      default: undefined
    },
    submenuItems: {
      type: Array,
      default: () => []
    }
  }
}
</script>

<style scoped>
.link {
  @apply flex w-full items-center rounded-md px-3 py-2 text-sm hover:bg-gray-100;
}
</style>
