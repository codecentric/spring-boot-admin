<template>
  <iframe
    class="flex w-full h-full"
    :src="params.url"
    sandbox="allow-scripts allow-same-origin"
    referrerpolicy="no-referrer"
    loading="lazy"
    :title="String(params.url)"
  ></iframe>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';

const { params } = useRoute();
</script>
