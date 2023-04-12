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
  <sba-navbar :brand="brand" class="text-sm lg:text-base">
    <sba-navbar-nav>
      <template v-for="item in topLevelViews" :key="item.id">
        <template v-if="item.children.length === 0">
          <sba-nav-item
            v-if="!item.href && item.name"
            :to="{ name: item.name }"
          >
            <component :is="item.handle" :error="error" />
          </sba-nav-item>
          <sba-nav-item
            v-else-if="item.href !== '#'"
            :href="item.href"
            target="blank"
          >
            <component :is="item.handle" :error="error" />
          </sba-nav-item>
          <sba-nav-item v-else>
            <component :is="item.handle" :error="error" />
          </sba-nav-item>
        </template>
        <template v-else>
          <sba-nav-dropdown :href="item.href">
            <template #label>
              <component :is="item.handle" />
            </template>
            <template #default>
              <sba-dropdown-item
                v-for="child in item.children"
                :key="child.name"
                v-bind="{ ...child }"
              >
                <component :is="child.handle" :error="error" />
              </sba-dropdown-item>
            </template>
          </sba-nav-dropdown>
        </template>
      </template>
    </sba-navbar-nav>
    <sba-navbar-nav class="ml-auto">
      <sba-nav-language-selector
        :available-locales="AVAILABLE_LANGUAGES"
        @locale-changed="changeLocale"
      />
      <sba-nav-usermenu />
      <sba-nav-item :to="{ name: 'about' }">
        <FontAwesomeIcon icon="question-circle" />
      </sba-nav-item>
    </sba-navbar-nav>
  </sba-navbar>
</template>

<script lang="ts" setup>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import moment from 'moment';
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import SbaDropdownItem from '@/components/sba-dropdown/sba-dropdown-item.vue';
import SbaNavDropdown from '@/components/sba-nav/sba-nav-dropdown.vue';
import SbaNavItem from '@/components/sba-nav/sba-nav-item.vue';
import SbaNavbarNav from '@/components/sba-navbar/sba-navbar-nav.vue';
import SbaNavbar from '@/components/sba-navbar/sba-navbar.vue';

import { useViewRegistry } from '@/composables/ViewRegistry';
import { AVAILABLE_LANGUAGES } from '@/i18n';
import sbaConfig from '@/sba-config';
import SbaNavLanguageSelector from '@/shell/sba-nav-language-selector.vue';
import SbaNavUsermenu from '@/shell/sba-nav-usermenu.vue';
import { compareBy } from '@/utils/collections';

defineProps({
  error: {
    type: Error,
    default: null,
  },
});

const { views } = useViewRegistry();
const i18n = useI18n();

const brand = sbaConfig.uiSettings.brand;

const topLevelViews = computed(() => {
  let rootViews = views
    .filter((view) => {
      return (
        !view.parent &&
        !view.name?.includes('instance') &&
        !view.name?.includes('about') &&
        !view.path?.includes('/instance')
      );
    })
    .sort(compareBy((v) => v.order));

  return rootViews.map((rootView) => {
    return {
      ...rootView,
      children: views.filter((v) => v.parent === rootView.name),
    };
  });
});

const changeLocale = (locale) => {
  i18n.locale.value = locale;
  moment.locale(locale);
};
</script>
