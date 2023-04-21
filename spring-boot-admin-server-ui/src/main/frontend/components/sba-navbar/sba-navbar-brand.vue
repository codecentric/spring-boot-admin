<template>
  <router-link class="brand" to="/" v-html="brand" />
</template>

<script lang="ts" setup>
defineProps({
  brand: {
    type: String,
    required: true,
  },
});
</script>

<style>
.brand {
  @apply flex items-center text-white mr-4;
}

.brand img {
  @apply h-8 w-8 mr-2;
}
</style>
