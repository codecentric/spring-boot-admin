<template>
  <a
    v-if="view.href"
    @click="onClick"
    :href="view.href"
    :class="{'navbar-item is-arrowless': !hasSubitems, 'navbar-link': hasSubitems}"
    target="_blank"
    rel="noopener noreferrer"
  >
    <component :is="view.handle" />
  </a>
  <router-link
    @click.native="onClick"
    v-else
    :data-sba-to="view.name"
    :to="{name: view.name}"
    active-class=""
    exact-active-class=""
    :class="{'navbar-item is-arrowless': !hasSubitems, 'navbar-link': hasSubitems}"
  >
    <component :is="view.handle" :applications="applications" :error="error" />
  </router-link>
</template>
<script>
export default {
  name: 'NavbarLink',
  props: {
    applications: {
      type: Array,
      default: () => [],
    },
    error: {
      type: Error,
      default: null
    },
    hasSubitems: {
      type: Boolean,
      default: false
    },
    view: {
      type: Object,
      required: true
    }
  },
  methods: {
    onClick($event) {
      $event?.target?.blur()
    }
  }
}
</script>
