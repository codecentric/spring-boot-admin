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
  <div class="flex h-full">
    <sba-wave />
    <div
      class="flex-shrink-0 flex flex-col transition-all duration-300 ease-in-out bg-white border-r relative overflow-hidden pb-14"
      :class="{
        'w-8': !sidebarOpen,
        'w-64 overflow-y-auto': sidebarOpen,
      }"
    >
      <button
        aria-label="Toggle sidebar"
        class="bg-sba-50 mx-[0.25rem] mt-[0.25rem] p-1 text-xs"
        :class="{
          'text-center': !sidebarOpen,
          'text-right': sidebarOpen,
        }"
        @click="toggleSidebar"
      >
        <font-awesome-icon
          :icon="faAngleDoubleLeft"
          class="transition-all"
          :class="{
            'rotate-0': sidebarOpen,
            'rotate-180': !sidebarOpen,
          }"
        />
      </button>
      <Sidebar
        v-if="instance"
        v-show="sidebarOpen"
        :key="instanceId"
        :application="application"
        :instance="instance"
        :views="sidebarViews"
      />
    </div>
    <main class="flex-1 overflow-y-auto relative">
      <router-view
        v-if="instance"
        :application="application"
        :instance="instance"
      />
    </main>
  </div>
</template>

<script lang="ts" setup>
import { faAngleDoubleLeft } from '@fortawesome/free-solid-svg-icons';
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';

import { useViewRegistry } from '@/composables/ViewRegistry';
import { useApplicationStore } from '@/composables/useApplicationStore';
import { findApplicationForInstance, findInstance } from '@/store';
import Sidebar from '@/views/instances/shell/sidebar';

const { applications } = useApplicationStore();
const { views } = useViewRegistry();
const route = useRoute();

const SIDEBAR_STORAGE_KEY = 'de.codecentric.spring-boot-admin.sidebar';
const sidebarOpen = ref(
  localStorage.getItem(SIDEBAR_STORAGE_KEY) === 'true' || true,
);

const instanceId = computed(() => {
  return route.params.instanceId;
});

const activeMainViewName = computed(() => {
  const currentView = route.meta.view;
  return currentView && (currentView.parent || currentView.name);
});

const sidebarViews = computed(() => {
  return views.filter((v) => v.parent === activeMainViewName.value);
});

const application = computed(() => {
  return findApplicationForInstance(applications.value, instanceId.value);
});

const instance = computed(() => {
  return findInstance(applications.value, instanceId.value);
});

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value;
  localStorage.setItem(SIDEBAR_STORAGE_KEY, String(sidebarOpen.value));
};
</script>
