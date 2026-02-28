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
    class="flex flex-col bg-white backdrop-filter backdrop-blur-lg bg-opacity-80 transition-all"
    :class="{ 'navbar--open': open }"
  >
    <ul class="relative px-1 pb-1">
      <!-- The actual nav -->
      <li
        v-for="group in groups"
        :key="group.name"
        :data-sba-group="group.id"
        class="relative mb-1"
      >
        <router-link
          :class="{
            'bg-sba-50 bg-opacity-80 text-sba-900': isActiveGroup(group),
            'px-1 justify-center': !open,
            'px-2 md:px-6': open,
          }"
          :to="{
            name: group.views[0].name,
            params: { instanceId: instance.id },
          }"
          class="bg-sba-50 bg-opacity-40 duration-300 ease-in-out flex items-center overflow-hidden py-4 rounded text-sm transition whitespace-nowrap text-gray-700 hover:bg-sba-50 hover:bg-opacity-80 hover:text-sba-900 h-12"
        >
          <span :class="{ '!m-0': !open }" v-html="group.icon" />
          <span
            v-if="open"
            v-text="
              hasMultipleViews(group)
                ? getGroupTitle(group.id)
                : $t(group.views[0].label)
            "
          />
          <svg
            v-if="hasMultipleViews(group) && open"
            :class="{
              '-rotate-90': !isActiveGroup(group),
              '': isActiveGroup(group),
            }"
            aria-hidden="true"
            class="h-3 ml-auto hidden md:block transition"
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
          v-if="hasMultipleViews(group) && isActiveGroup(group) && open"
          class="relative block"
        >
          <li
            v-for="view in group.views"
            :key="view.name"
            :data-sba-view="view.name"
          >
            <router-link
              :to="{ name: view.name, params: { instanceId: instance.id } }"
              active-class="bg-sba-50 bg-opacity-80 text-sba-900"
              class="bg-sba-50 bg-opacity-40 duration-300 ease-in-out flex items-center overflow-hidden py-4 rounded text-sm transition whitespace-nowrap text-gray-700 hover:bg-sba-50 hover:bg-opacity-80 hover:text-sba-900 h-6 mb-1 mt-1 pl-12 pr-6"
              exact-active-class=""
            >
              <component :is="view.handle" />
            </router-link>
          </li>
        </ul>
      </li>

      <template v-if="customLinksFromMetadata?.length > 0 && open">
        <Divider align="center" class="!my-2">
          <small class="bold">
            {{ $t('sidebar.custom-link.title') }}
          </small>
        </Divider>

        <li>
          <ul class="relative block">
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
                active-class="bg-sba-50 bg-opacity-80 text-sba-900"
                class="bg-sba-50 bg-opacity-40 duration-300 ease-in-out flex items-center overflow-hidden py-4 rounded text-sm transition whitespace-nowrap text-gray-700 hover:bg-sba-50 hover:bg-opacity-80 hover:text-sba-900 h-6 mb-1 mt-1 pl-12 pr-6 !px-6"
                exact-active-class=""
              >
                <component :is="view.handle" />
              </router-link>
              <a
                v-else-if="view.href"
                :href="view.href"
                class="bg-sba-50 bg-opacity-40 duration-300 ease-in-out flex items-center overflow-hidden py-4 rounded text-sm transition whitespace-nowrap text-gray-700 hover:bg-sba-50 hover:bg-opacity-80 hover:text-sba-900 h-6 mb-1 mt-1 pl-12 pr-6 !px-6"
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
import { faArrowUpRightFromSquare } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { Divider } from 'primevue';
import { computed, h, toRaw } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';

import Application from '@/services/application';
import Instance from '@/services/instance';
import { compareBy } from '@/utils/collections';
import { VIEW_GROUP, VIEW_GROUP_ICON } from '@/views/ViewGroup';

const props = withDefaults(
  defineProps<{
    views: any[];
    instance: Instance;
    application: Application;
    open?: boolean;
  }>(),
  {
    open: true,
  },
);

const { t } = useI18n();
const route = useRoute();
defineEmits(['toggle-sidebar']);

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

function getGroupTitle(groupId: string) {
  const key = 'sidebar.' + groupId + '.title';
  const translated = t(key);
  return key === translated ? groupId : translated;
}

function isActiveGroup(group: any) {
  if (group.id === VIEW_GROUP.CUSTOM_LINK) {
    return true;
  }

  const result = group.views.some(
    (view: any) => toRaw(view) === route.meta.view,
  );
  return result;
}

function hasMultipleViews(group: any) {
  return group.views.length > 1;
}
</script>
