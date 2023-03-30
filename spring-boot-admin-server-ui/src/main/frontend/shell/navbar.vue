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
  <sba-navbar :brand="brand">
    <sba-navbar-nav>
      <template v-for="item in topLevelViews" :key="item.name">
        <sba-nav-item v-if="!item.href" :to="{ name: item.name }">
          <component :is="item.handle" :error="error" />
        </sba-nav-item>
        <sba-nav-item v-else :href="item.href" target="blank">
          <component :is="item.handle" :error="error" />
        </sba-nav-item>
      </template>
    </sba-navbar-nav>
    <sba-navbar-nav class="ml-auto">
      <sba-nav-language-selector
        :available-locales="AVAILABLE_LANGUAGES"
        @localeChanged="changeLocale"
      />
      <sba-nav-usermenu />
    </sba-navbar-nav>
  </sba-navbar>
</template>

<script lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import moment from 'moment';
import { defineComponent } from 'vue';

import SbaDropdownDivider from '@/components/sba-dropdown/sba-dropdown-divider.vue';
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

export default defineComponent({
  components: {
    SbaNavLanguageSelector,
    SbaNavUsermenu,
    FontAwesomeIcon,
    SbaNavItem,
    SbaNavbarNav,
    SbaNavDropdown,
    SbaDropdownDivider,
    SbaDropdownItem,
    SbaNavbar,
  },
  props: {
    error: {
      type: Error,
      default: null,
    },
  },
  setup() {
    const { views } = useViewRegistry();

    return {
      views,
      AVAILABLE_LANGUAGES,
    };
  },
  data: () => ({
    showMenu: false,
    brand:
      '<img src="assets/img/icon-spring-boot-admin.svg"><span>Spring Boot Admin</span>',
    username: '',
  }),
  computed: {
    enabledViews() {
      return this.topLevelViews
        .filter(
          (view) => typeof view.isEnabled === 'undefined' || view.isEnabled()
        )
        .sort(compareBy((v) => v.order));
    },
    topLevelViews(): SbaViewDescriptor[] {
      return this.views
        .filter(
          (view) =>
            !view.name.includes('instance') && !view.path?.includes('/instance')
        )
        .sort(compareBy((v) => v.order));
    },
    userSubMenuItems() {
      return this.enabledViews.filter((v) => v.parent === 'user');
    },
  },
  created() {
    this.brand = sbaConfig.uiSettings.brand;
    this.availableLocales = AVAILABLE_LANGUAGES;
  },
  methods: {
    changeLocale(locale) {
      this.$i18n.locale = locale;
      moment.locale(this.$i18n.locale);
    },
  },
});
</script>
