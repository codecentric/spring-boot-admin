<template>
  <div class="link-wrapper">
    <a
      v-if="view.href && view.handle"
      :key="view.name"
      :href="view.href"
      target="_blank"
      class="link"
      :class="classes"
      rel="noopener noreferrer"
    >
      <component :is="view.handle" :error="error" />
    </a>

    <div
      v-else-if="view.label && !view.handle"
      :key="view.label"
      class="link"
      :class="classes"
    >
      <div>{{ view.label }}</div>
    </div>

    <router-link
      v-else
      :key="view.name"
      class="link"
      :class="classes"
      active-class="link--active"
      :to="{ name: view.name }"
    >
      <component :is="view.handle" :error="error" />
    </router-link>
    <NavbarMenu
      v-if="hasSubitems"
      :menu-items="subitems"
      @menu-item-clicked="$emit('menuItemClicked', $event)"
    />
  </div>
</template>

<script>
import { computed } from 'vue';

import NavbarMenu from '@/shell/NavbarMenu';

export default {
  components: { NavbarMenu },
  props: {
    error: {
      type: Error,
      default: null,
    },
    view: {
      type: Object,
      required: true,
    },
    subitems: {
      type: Array,
      default: () => [],
    },
  },
  emits: ['menuItemClicked'],
  setup(props) {
    const hasSubitems = computed({
      get() {
        return props.subitems.length > 0;
      },
    });

    const classes = computed({
      get() {
        return props.subitems.length > 0 ? ['link--has-submenu'] : [];
      },
    });

    return {
      classes,
      hasSubitems,
    };
  },
};
</script>

<style>
.link-wrapper {
  @apply hover:bg-sba-700 relative flex items-center rounded-md;
}

.link {
  @apply flex w-full items-center px-3 py-2 hover:bg-sba-700 rounded-l-md cursor-pointer;
}

.link:not(.link--has-submenu) {
  @apply rounded-r-md;
}

.link + .submenu .submenu-opener-button {
  @apply py-2 pr-2 rounded-r-md;
}

.link--active,
.link-wrapper:hover {
  @apply bg-sba-700;
}

.link-wrapper:hover .submenu .submenu-opener-button,
.link--active + .submenu .submenu-opener-button {
  @apply bg-sba-800;
}

.submenu .link {
  @apply hover:bg-sba-700 hover:text-white;
}

.submenu .link--active {
  @apply bg-sba-200;
}
</style>
