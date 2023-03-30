<template>
  <a v-if="href" :href="href" class="sba-nav-item">
    <slot />
  </a>
  <router-link v-if="to" :to="to" class="sba-nav-item">
    <slot />
  </router-link>
</template>

<script lang="ts" setup>
defineProps({
  href: {
    type: String,
    default: null,
  },
  to: {
    type: String,
    default: null,
  },
});
</script>

<style scoped>
.sba-nav-item {
  @apply px-3 py-2 rounded hover:bg-sba-700;
}
</style>
<style>
.sba-nav-item.is-active {
  @apply bg-sba-700;
}
</style>
