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
      class="flex-shrink-0 flex flex-col transition-all duration-300 ease-in-out bg-white border-r relative overflow-hidden"
      :class="{
        'w-10': !sidebarOpen,
        'w-64': sidebarOpen,
      }"
    >
      <div v-if="instance" class="px-1 py-1">
        <div
          v-if="!sidebarOpen"
          class="rounded-full bg-sba-50 text-center aspect-square flex items-center overflow-hidden text-xs"
        >
          {{ createInitials(instance.registration.name) }}
        </div>
        <div
          v-if="sidebarOpen"
          class="relative hidden md:block bg-sba-50 bg-opacity-40 text-sba-900 text-sm py-4 pl-6 pr-2 text-left overflow-hidden text-ellipsis rounded transition duration-300 ease-in-out cursor-pointer"
        >
          <router-link
            :to="{
              name: 'instances/details',
              params: { instanceId: instance.id },
            }"
          >
            <span class="overflow-hidden text-ellipsis">
              <div class="font-bold" v-text="instance.registration.name" />
              <div>
                <small><em v-text="instance.id" /></small>
              </div>
            </span>
          </router-link>
          <button
            class="absolute top-1 right-1 p-1 rounded focus:outline-none focus:ring focus:ring-sba-300"
            @click="toggleSidebar"
          >
            t
          </button>
        </div>
      </div>
      <div class="fex-1 overflow-y-auto">
        <Sidebar
          v-if="instance"
          :key="instanceId"
          :open="sidebarOpen"
          :application="application"
          :instance="instance"
          :views="sidebarViews"
        />
      </div>
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
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';

import { useViewRegistry } from '@/composables/ViewRegistry';
import { useApplicationStore } from '@/composables/useApplicationStore';
import { findApplicationForInstance, findInstance } from '@/store';
import { createInitials } from '@/utils/createInitials';
import Sidebar from '@/views/instances/shell/sidebar.vue';

const { applications } = useApplicationStore();
const { views } = useViewRegistry();
const route = useRoute();

const SIDEBAR_STORAGE_KEY = 'de.codecentric.spring-boot-admin.sidebar';
const sidebarOpen = ref<boolean>(
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
