<!--
  - Copyright 2014-2020 the original author or authors.
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
  <aside
    :class="{ 'w-60': sidebarOpen }"
    class="h-full flex flex-col bg-white/80 border-r border-gray-200 backdrop-filter backdrop-blur-lg z-40 w-10 md:w-60 transition-all left-0 pb-14 fixed"
  >
    <ul class="relative px-1 py-1 overflow-y-auto">

      <!-- Instance info block -->
      <li class="relative mb-1 hidden md:block">
        <router-link
          :class="`instance-summary--${instance.statusInfo.status}`"
          :to="{
            name: 'instances/details',
            params: { instanceId: instance.id },
          }"
          class="instance-info-block"
        >
          <span class="overflow-hidden text-ellipsis">
            <span class="font-bold" v-text="instance.registration.name" /><br />
            <small><em v-text="instance.id" /></small>
          </span>
        </router-link>
      </li>

      <!-- Mobile toggle -->
      <li class="block md:hidden mb-1">
        <a class="navbar-link navbar-link__group" @click.stop="toggleSidebar">
          <font-awesome-icon :icon="['fas', 'bars']" />
        </a>
      </li>

      <!-- NAV -->
      <li
        v-for="group in groups"
        :key="group.name"
        :data-sba-group="group.id"
        class="relative mb-1"
      >
        <router-link
          :class="{ 'navbar-link__active': isActiveGroup(group) }"
          :to="{
            name: group.views[0].name,
            params: { instanceId: instance.id },
          }"
          class="navbar-link navbar-link__group"
          @click.prevent="toggleGroup(group.id)"
        >
          <span v-html="group.icon" />

          <span
            v-text="
              hasMultipleViews(group)
                ? getGroupTitle(group.id)
                : $t(group.views[0].label)
            "
          />

          <svg
            v-if="hasMultipleViews(group)"
            :class="{
              '-rotate-90': !(openGroup === group.id || isActiveGroup(group)),
            }"
            class="h-3 ml-auto hidden md:block transition"
            viewBox="0 0 448 512"
          >
            <path
              d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z"
              fill="currentColor"
            />
          </svg>
        </router-link>

        <!-- SUBMENU -->
        <ul
          v-if="hasMultipleViews(group) && (openGroup === group.id || isActiveGroup(group))"
          :class="{ 'hidden md:block': !sidebarOpen }"
          class="relative block"
        >
          <li
            v-for="view in group.views"
            :key="view.name"
          >
            <router-link
              :to="{ name: view.name, params: { instanceId: instance.id } }"
              active-class="navbar-link__active"
              class="navbar-link navbar-link__group_item"
            >
              <component :is="view.handle" />
            </router-link>
          </li>
        </ul>
      </li>

    </ul>
  </aside>
</template>

<script lang="ts" setup>
import { computed, h, ref, toRaw, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';
import { VIEW_GROUP, VIEW_GROUP_ICON } from '@/views/ViewGroup';

const props = defineProps<any>();

const { t } = useI18n();
const route = useRoute();

const sidebarOpen = ref(false);
const openGroup = ref<string | null>(null); 

function toggleGroup(groupId: string) {
  openGroup.value = openGroup.value === groupId ? null : groupId;
}

watch(() => route.fullPath, () => {
  sidebarOpen.value = false;
  openGroup.value = null; 
});

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value;
}

function isActiveGroup(group: any) {
  if (group.id === VIEW_GROUP.CUSTOM_LINK) return true;

  return group.views.some(
    (view: any) => toRaw(view) === route.meta.view,
  );
}

function hasMultipleViews(group: any) {
  return group.views.length > 1;
}

function getGroupTitle(groupId: string) {
  const key = 'sidebar.' + groupId + '.title';
  const translated = t(key);
  return key === translated ? groupId : translated;
}

const groups = computed(() => {
  const groups = new Map();

  props.views.forEach((view: any) => {
    const groupName = view.group;
    const group = groups.get(groupName) || {
      id: groupName,
      views: [],
    };

    groups.set(groupName, {
      ...group,
      icon: VIEW_GROUP_ICON[groupName],
      views: [...group.views, view],
    });
  });

  return Array.from(groups.values());
});
</script>