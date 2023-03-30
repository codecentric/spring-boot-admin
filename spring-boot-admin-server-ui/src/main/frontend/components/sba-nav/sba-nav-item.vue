<template>
  <a :href="href" class="sba-nav-item">
    <slot />
  </a>
</template>

<script lang="ts" setup>
defineProps({
  href: {
    type: String,
    required: false,
  },
});
</script>

<style scoped>
.sba-nav-item {
  @apply px-3 py-2 rounded hover:bg-sba-700;
}
</style>
