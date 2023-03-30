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
  <nav
    id="navigation"
    class="bg-black fixed flex flex-wrap justify-between mx-auto top-0 w-full z-50 items-center mx-auto px-4 py-3 sm:px-6 lg:px-8 text-white"
  >
    <sba-navbar-brand :brand="brand" />

    <sba-navbar-toggle @click="toggleNavigation" />

    <div
      :class="{ block: showNavigation, hidden: !showNavigation }"
      class="nav-container lg:block"
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

<style>
.brand {
  @apply inline-flex items-center;
}

.brand img {
  @apply h-8 w-8 mr-2;
}

.brand span {
  @apply font-semibold text-xl;
}

.nav-container {
  @apply justify-between flex-grow items-center mt-4 mb-2 p-2 lg:p-0;
  @apply lg:flex lg:mt-0 lg:mb-0;
}
</style>
