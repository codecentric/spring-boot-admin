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

      <!-- sm: button toggle navigation -->
      <li class="block md:hidden mb-1">
        <a class="navbar-link navbar-link__group" @click.stop="toggleSidebar">
          <font-awesome-icon :icon="['fas', 'bars']" />
        </a>
      </li>

      <!-- The actual nav -->
      <li
        v-for="group in groups"
        :key="group.name"
        :data-sba-group="group.id"
        class="relative mb-1"
      >
        <router-link
          :to="{
            name: group.views[0].name,
            params: { instanceId: instance.id },
          }"
          class="navbar-link navbar-link__group"
        >
          <span v-html="group.icon" />
          <span
            v-text="
              hasMultipleViews(group)
                ? getGroupTitle(group.id)
                : $t(group.views[0].label)
            "
          />
          <button
            v-if="hasMultipleViews(group)"
            class="ml-auto px-2 py-2 cursor-pointer hover:bg-sba-500/50 rounded"
            @click.prevent="toggleGroup(group.id)"
          >
            <font-awesome-icon
              class="h-3 ml-auto hidden md:block transition"
              :class="{
                '-rotate-90': isGroupOpen(group),
              }"
              :icon="faChevronUp"
            />
          </button>
        </router-link>

        <!-- Le subnav -->
        <ul
          v-if="hasMultipleViews(group) && isGroupOpen(group)"
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

      <template v-if="customLinksFromMetadata?.length > 0">
        <Divider align="center" class="my-2!">
          <small class="bold">
            {{ $t('sidebar.custom-link.title') }}
          </small>
        </Divider>

        <li>
          <ul
            :class="{ 'hidden md:block': !sidebarOpen }"
            class="relative block"
          >
            <li
              v-for="view in customLinksFromMetadata"
              :key="view.name"
              :data-sba-view="view.name"
            >
              <router-link
                v-if="view.iframe"
                :to="{
                  name: 'instances/custom-link-view',
                  params: { instanceId: instance.id, url: view.href },
                }"
                active-class="navbar-link__active"
                class="navbar-link navbar-link--custom navbar-link__group_item"
                exact-active-class=""
              >
                <component :is="view.handle" />
              </router-link>
              <a
                v-else-if="view.href"
                :href="view.href"
                class="navbar-link navbar-link--custom navbar-link__group_item"
                target="_blank"
                rel="noopener"
              >
                <component :is="view.handle" />
                <font-awesome-icon
                  :icon="faArrowUpRightFromSquare"
                  class="w-2 ml-1"
                />
              </a>
            </li>
          </ul>
        </li>
      </template>
    </ul>
  </aside>
</template>

<script lang="ts" setup>
import {
  faArrowUpRightFromSquare,
  faChevronUp,
} from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { Divider } from 'primevue';
import { computed, h, ref, toRaw, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';

import Application from '@/services/application';
import Instance from '@/services/instance';
import { compareBy } from '@/utils/collections';
import { VIEW_GROUP_ICON } from '@/views/ViewGroup';

const props = defineProps<{
  views: any[];
  instance: Instance;
  application: Application;
}>();

const { t } = useI18n();
const route = useRoute();
const sidebarOpen = ref(false);
const openGroup = ref<string | null>(null);

const customLinksFromMetadata = computed(() => {
  const newVar = props.instance.metadataParsed?.sidebar?.links || [];
  return newVar.map((view) => {
    return {
      handle: () => h('span', { innerHTML: view.label }),
      href: view.url,
      iframe: view.iframe || false,
      order: Number.MAX_SAFE_INTEGER,
    } satisfies SbaView;
  });
});

const enabledViews = computed(() => {
  if (!props.instance) {
    return [];
  }
  return [...props.views]
    .filter(
      (view) =>
        typeof view.isEnabled === 'undefined' ||
        view.isEnabled({ instance: props.instance }),
    )
    .sort(compareBy((v: any) => v.order));
});

const groups = computed(() => {
  const groups = new Map();

  [...enabledViews.value].forEach((view: SbaView) => {
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
});

watch(
  [() => route.fullPath, groups],
  () => {
    sidebarOpen.value = false;

    const activeGroup = groups.value.find((group) =>
      group.views.some((view: SbaView) => toRaw(view) === route.meta.view),
    );

    openGroup.value = activeGroup?.id ?? null;
  },
  { immediate: true },
);

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value;
}

function toggleGroup(groupId: string) {
  openGroup.value = openGroup.value === groupId ? null : groupId;
}

function getGroupTitle(groupId: string) {
  const key = 'sidebar.' + groupId + '.title';
  const translated = t(key);
  return key === translated ? groupId : translated;
}

function isGroupOpen(group: any) {
  return openGroup.value === group.id;
}

function hasMultipleViews(group: any) {
  return group.views.length > 1;
}
</script>

<style scoped>
@reference "../../../index.css";

.instance-info-block {
  @apply bg-sba-50/40 text-sba-900 flex items-center text-sm py-4 px-6 text-left overflow-hidden text-ellipsis rounded transition duration-300 ease-in-out cursor-pointer;
}

a.navbar-link {
  @apply cursor-pointer;
}
.navbar-link {
  @apply bg-sba-50/40 duration-300 ease-in-out flex  items-center overflow-hidden py-4 rounded text-sm transition whitespace-nowrap;
  @apply text-gray-700;
}
.navbar-link--custom {
  @apply !px-6;
}

.navbar-link:hover,
.navbar-link__active {
  @apply bg-sba-50/80 text-sba-900;
}

.navbar-link__group_item {
  @apply h-6 mb-1 mt-1 pl-12 pr-6;
}

.navbar-link__group {
  @apply h-12 px-2 md:pl-6;
}
</style>
