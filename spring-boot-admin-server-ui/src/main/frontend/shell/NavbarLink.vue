<template>
  <a
    v-if="view.href && view.handle"
    :key="view.name"
    :href="view.href"
    target="_blank"
    class="link"
    rel="noopener noreferrer"
  >
    <component :is="view.handle" />
    <font-awesome-icon
      v-if="hasSubitems"
      class="ml-2"
      :icon="['fas', 'chevron-down']"
    />
  </a>
  <div
    v-else-if="view.label && !view.handle"
    :key="view.name"
    class="link"
  >
    <div>{{ view.label }}</div>
    <font-awesome-icon
      v-if="hasSubitems"
      class="ml-2"
      :icon="['fas', 'chevron-down']"
    />
  </div>
  <router-link
    v-else
    :key="view.name"
    class="link"
    active-class="link--active"
    :to="{name: view.name}"
  >
    <component
      :is="view.handle"
      :error="error"
    />
    <font-awesome-icon
      v-if="hasSubitems"
      class="ml-2"
      :icon="['fas', 'chevron-down']"
    />
  </router-link>
</template>

<script>
export default {
  name: 'NavbarLink',
  props: {
    error: {
      type: Error,
      default: null
    },
    view: {
      type: Object,
      required: true
    },
    hasSubitems: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style scoped>
.link {
  @apply flex w-full items-center rounded-md text-sba-900 px-3 py-2 text-inherit text-sm hover:bg-sba-700 transition-colors;
}
.link--active {
  @apply bg-sba-700;
}
</style>
