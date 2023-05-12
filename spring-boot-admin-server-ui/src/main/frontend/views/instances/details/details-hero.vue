<template>
  <div class="px-6 pt-10 pb-5">
    <div class="m-auto text-center">
      <h1
        class="text-2xl md:text-4xl text-center font-bold text-gray-900 tracking-tight"
        v-text="instance.registration.name"
      />
      <small><em v-text="instance.id" /></small>
    </div>
    <div class="mt-4 text-center">
      <sba-tags v-if="instance" :tags="instance.tags" class="justify-center" />
    </div>
  </div>
</template>

<script>
import Instance from '@/services/instance';

export default {
  name: 'DetailsHero',
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
};
</script>

<style scoped></style>
