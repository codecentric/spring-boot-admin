<template>
  <template v-for="view in topMenuItems" :key="view.name">
    <NavbarLink :error="error" :view="view" :subitems="subMenuItems(view)" />
  </template>
</template>

<script>
import NavbarLink from '@/shell/NavbarLink';

export default {
  name: 'NavbarItems',
  components: { NavbarLink },
  props: {
    error: {
      type: Error,
      default: null,
    },
    enabledViews: {
      type: Array,
      default: () => [],
    },
  },
  computed: {
    topMenuItems() {
      return this.enabledViews.filter((view) => !view.parent);
    },
  },
  methods: {
    subMenuItems(parentView) {
      return this.enabledViews.filter(
        (view) => view.parent === parentView.name
      );
    },
  },
};
</script>
