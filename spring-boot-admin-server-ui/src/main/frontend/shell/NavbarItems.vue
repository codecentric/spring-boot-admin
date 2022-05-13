<template>
  <template
    v-for="view in topMenuItems"
    :key="view.name"
  >
    <NavbarMenu
      button-type="div"
      :menu-items="subMenuItems(view)"
    >
      <template #default="slotProps">
        <NavbarLink
          :has-subitems="slotProps.hasSubitems"
          :applications="applications"
          :error="error"
          :view="view"
        />
      </template>

      <template #menuItems>
        <MenuItem
          v-for="menuItem in subMenuItems(view)"
          :key="menuItem.name"
          class="flex"
        >
          <NavbarLink
            :applications="applications"
            :error="error"
            :view="menuItem"
          />
        </MenuItem>
      </template>
    </NavbarMenu>
  </template>
</template>

<script>
import NavbarMenu from "./navbar-menu.vue"
import NavbarLink from "./NavbarLink.vue";
import {MenuItem} from "@headlessui/vue";

export default {
  name: 'NavbarItems',
  components: {NavbarLink, NavbarMenu, MenuItem},
  props: {
    applications: {
      type: Array,
      default: () => [],
    },
    error: {
      type: Error,
      default: null
    },
    enabledViews: {
      type: Array,
      default: () => [],
    }
  },
  computed: {
    topMenuItems() {
      return this.enabledViews.filter(view => !view.parent);
    }
  },
  methods: {
    subMenuItems(menu) {
      return this.enabledViews.filter(view => view.parent === menu.name);
    }
  }
}
</script>
