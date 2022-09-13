<template>
  <template
    v-for="view in topMenuItems"
    :key="view.name"
  >
    <NavbarLink
      :error="error"
      :view="view"
      :subitems="subMenuItems(view)"
    />
  </template>
</template>

<script>
import NavbarLink from "./NavbarLink.vue";
import {MenuItem} from "@headlessui/vue";

export default {
  name: 'NavbarItems',
  components: {NavbarLink, MenuItem},
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
    subMenuItems(parentView) {
      return this.enabledViews.filter(view => view.parent === parentView.name);
    }
  }
}
</script>
