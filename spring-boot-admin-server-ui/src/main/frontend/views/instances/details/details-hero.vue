<template>
  <div class="px-6 pt-10 pb-5">
    <div class="m-auto text-center">
      <h1
        class="text-2xl md:text-4xl text-center font-bold text-gray-900 tracking-tight"
        v-text="instance?.registration?.name"
      />
      <small><em v-text="instanceId" /></small>
    </div>
    <div class="mt-4 text-center">
      <sba-tags
        v-if="instance?.tags"
        :tags="instance.tags"
        class="justify-center"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useInstanceData } from '@/composables/useInstanceData';

const props = defineProps<{
  instanceId: string;
}>();

const { instance } = useInstanceData(props.instanceId);
</script>

<style scoped></style>
@reference "../../../index.css";
