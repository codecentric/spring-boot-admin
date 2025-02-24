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
    class="h-full flex flex-col bg-white border-r backdrop-filter backdrop-blur-lg bg-opacity-80 z-40 w-10 md:w-60 transition-all left-0 pb-14 fixed"
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

      <!-- sm: button toggle navigation -->
      <li class="block md:hidden">
        <a class="navbar-link navbar-link__group" @click.stop="toggleSidebar">
          <font-awesome-icon :icon="['fas', 'bars']" />
        </a>
      </li>

      <!-- The actual nav -->
      <li
        v-for="group in enabledGroupedViews"
        :key="group.name"
        :data-sba-group="group.id"
        class="relative"
      >
        <router-link
          :class="{ 'navbar-link__active': isActiveGroup(group) }"
          :to="{
            name: group.views[0].name,
            params: { instanceId: instance.id },
          }"
          active-class=""
          class="navbar-link navbar-link__group"
          exact-active-class=""
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
              '-rotate-90': !isActiveGroup(group),
              '': isActiveGroup(group),
            }"
            aria-hidden="true"
            class="h-3 ml-auto hidden md:block"
            data-prefix="fas"
            focusable="false"
            role="img"
            viewBox="0 0 448 512"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z"
              fill="currentColor"
            />
          </svg>
        </router-link>

        <!-- Le subnav -->
        <ul
          v-if="hasMultipleViews(group) && isActiveGroup(group)"
          :class="{ 'hidden md:block': !sidebarOpen }"
          class="relative block"
        >
          <li
            v-for="view in group.views"
            :key="view.name"
            :data-sba-view="view.name"
          >
            <router-link
              :to="{ name: view.name, params: { instanceId: instance.id } }"
              active-class="navbar-link__active"
              class="navbar-link navbar-link__group_item"
              exact-active-class=""
            >
              <component :is="view.handle" />
            </router-link>
          </li>
        </ul>
      </li>
    </ul>
  </aside>
</template>

<script lang="ts">
import { defineComponent, toRaw } from 'vue';

import SbaButton from '@/components/sba-button.vue';

import Application from '@/services/application';
import Instance from '@/services/instance';
import { compareBy } from '@/utils/collections';
import { VIEW_GROUP_ICON } from '@/views/ViewGroup';

export default defineComponent({
  components: { SbaButton },
  props: {
    views: {
      type: Array,
      default: () => [],
    },
    instance: {
      type: Instance,
      default: null,
    },
    application: {
      type: Application,
      default: null,
    },
  },
  data() {
    return {
      sidebarOpen: false,
    };
  },
  computed: {
    enabledViews() {
      if (!this.instance) {
        return [];
      }

      return [...this.views]
        .filter(
          (view) =>
            typeof view.isEnabled === 'undefined' ||
            view.isEnabled({ instance: this.instance }),
        )
        .sort(compareBy((v) => v.order));
    },
    enabledGroupedViews() {
      const groups = new Map();
      this.enabledViews.forEach((view) => {
        const groupName = view.group;
        const group = groups.get(groupName) || {
          id: groupName,
          order: Number.MAX_SAFE_INTEGER,
          views: [],
        };
        groups.set(groupName, {
          ...group,
          order: Math.min(group.order, view.order),
          icon: VIEW_GROUP_ICON[groupName],
          views: [...group.views, view],
        });
      });
      return Array.from(groups.values());
    },
  },
  watch: {
    $route() {
      this.sidebarOpen = false;
    },
  },
  methods: {
    toggleSidebar() {
      this.sidebarOpen = !this.sidebarOpen;
    },
    getGroupTitle(groupId) {
      const key = 'sidebar.' + groupId + '.title';
      const translated = this.$t(key);
      return key === translated ? groupId : translated;
    },
    isActiveGroup(group) {
      return group.views.some((v) => toRaw(v) === this.$route.meta.view);
    },
    hasMultipleViews(group) {
      return group.views.length > 1;
    },
  },
});
</script>

<style scoped>
.instance-info-block {
  @apply bg-sba-50 bg-opacity-40 text-sba-900 flex items-center text-sm py-4 px-6 text-left overflow-hidden text-ellipsis rounded transition duration-300 ease-in-out cursor-pointer;
}

.navbar-link {
  @apply cursor-pointer bg-sba-50 bg-opacity-40 duration-300 ease-in-out flex  items-center overflow-hidden py-4 rounded text-sm transition whitespace-nowrap;
  @apply text-gray-700;
}

.navbar-link:hover,
.navbar-link__active {
  @apply bg-sba-50 bg-opacity-80 text-sba-900;
}

.navbar-link__group_item {
  @apply h-6 mb-1 mt-1 pl-12 pr-6;
}

.navbar-link__group {
  @apply h-12 px-2 md:px-6;
}
</style>
