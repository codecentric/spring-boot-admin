<!--
  - Copyright 2014-2019 the original author or authors.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <nav id="navigation" class="sba-navbar">
    <sba-navbar-brand :brand="brand" />

    <sba-navbar-toggle @click="toggleNavigation" />

    <div
      :class="{ 'nav-container--hidden': !showNavigation }"
      class="nav-container"
    >
      <slot />
    </div>
  </nav>
</template>

<script lang="ts" setup>
import { ref } from 'vue';

import SbaNavbarBrand from '@/components/sba-navbar/sba-navbar-brand.vue';
import SbaNavbarToggle from '@/components/sba-navbar/sba-navbar-toggle.vue';

defineProps({
  brand: {
    type: String,
    required: true,
  },
});

const showNavigation = ref(false);

function toggleNavigation() {
  showNavigation.value = !showNavigation.value;
}
</script>

<style scoped>
.sba-navbar {
  @apply bg-black fixed flex flex-wrap justify-start mx-auto top-0 w-full z-50 items-center px-4 py-2 text-white;
  @apply sm:px-6;
  @apply xl:px-8;
}
.nav-container {
  @apply flex-grow block w-full flex-row mt-4;
  @apply lg:w-auto lg:flex xl:mt-0;
}
.nav-container--hidden {
  @apply hidden;
  @apply xl:flex;
}
</style>
