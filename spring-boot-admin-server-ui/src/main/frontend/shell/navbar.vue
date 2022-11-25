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
  <nav id="navigation" class="bg-black fixed top-0 w-full h-14 z-50">
    <div class="mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-14">
        <div class="flex">
          <div class="flex flex-shrink-0 text-white mr-6">
            <router-link class="brand" to="/" v-html="brand" />
          </div>

          <div class="hidden lg:block">
            <div class="navbar-top-menu">
              <NavbarItems :enabled-views="enabledViews" :error="error" />
            </div>
          </div>
        </div>

        <div class="hidden lg:block">
          <div class="ml-4 flex items-center md:ml-6 gap-4 text-white">
            <NavbarItemLanguageSelector
              v-if="availableLocales.length > 1"
              :current-locale="$i18n.locale"
              :available-locales="availableLocales"
              @locale-changed="changeLocale"
            />

            <NavbarUserMenu
              v-if="userName"
              :csrf-parameter-name="csrfParameterName"
              :csrf-token="csrfToken"
              :user-name="userName"
              :submenu-items="userSubMenuItems"
            />
          </div>
        </div>

        <!-- Mobile menu button -->
        <div class="-mr-2 flex lg:hidden">
          <button
            type="button"
            class="bg-gray-800 inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-white hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-800 focus:ring-white"
            aria-controls="mobile-menu"
            :aria-expanded="showMenu"
            @click.stop="showMenu = !showMenu"
          >
            <span class="sr-only">Open main menu</span>
            <svg
              :class="{ block: !showMenu, hidden: showMenu }"
              class="h-6 w-6"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              aria-hidden="true"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M4 6h16M4 12h16M4 18h16"
              />
            </svg>
            <svg
              :class="{ block: showMenu, hidden: !showMenu }"
              class="h-6 w-6"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              aria-hidden="true"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Mobile menu, show/hide based on menu state. -->
    <div v-if="showMenu" id="mobile-menu" class="lg:hidden bg-black">
      <div class="px-2 pt-2 pb-3 space-y-1 sm:px-3">
        <!-- LINKS -->
        <template v-for="view in enabledViews">
          <a
            v-if="view.href"
            :key="view.name"
            :href="view.href"
            class="text-gray-300 hover:bg-gray-700 hover:text-white block px-3 py-2 rounded-md text-base font-medium"
            target="_blank"
            rel="noopener noreferrer"
          >
            <component :is="view.handle" />
          </a>
          <router-link
            v-else
            :key="view.name"
            :to="{ name: view.name }"
            class="text-gray-300 hover:bg-gray-700 hover:text-white block px-3 py-2 rounded-md text-base font-medium"
          >
            <component :is="view.handle" :error="error" />
          </router-link>
        </template>
        <!-- LINKS -->
      </div>

      <div v-if="userName" class="pt-4 pb-3 border-t">
        <div class="flex items-center px-5">
          <div class="flex-shrink-0">
            <font-awesome-icon
              color="white"
              class="h-10 w-10 rounded-full white"
              icon="user-circle"
              size="2x"
            />
          </div>
          <div class="ml-3">
            <div
              class="text-base font-medium leading-none text-white"
              v-text="userName"
            />
          </div>
        </div>
        <div class="mt-3 px-2 space-y-1 text-black">
          <NavbarLink
            v-for="userSubMenuItem in userSubMenuItems"
            :key="userSubMenuItem.name"
            :error="error"
            :view="userSubMenuItem"
          />

          <form action="logout" method="post">
            <input
              v-if="csrfToken"
              type="hidden"
              :name="csrfParameterName"
              :value="csrfToken"
            />
            <button
              type="submit"
              class="text-gray-300 hover:bg-gray-700 hover:text-white px-3 text-left py-2 rounded-md font-medium w-full"
              value="logout"
            >
              <font-awesome-icon icon="sign-out-alt" />&nbsp;<span
                v-text="$t('navbar.logout')"
              />
            </button>
          </form>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
import moment from 'moment';

import { useViewRegistry } from '@/composables/ViewRegistry';
import { AVAILABLE_LANGUAGES } from '@/i18n';
import sbaConfig from '@/sba-config';
import NavbarItems from '@/shell/NavbarItems';
import NavbarLink from '@/shell/NavbarLink';
import NavbarUserMenu from '@/shell/NavbarUserMenu';
import NavbarItemLanguageSelector from '@/shell/navbar-item-language-selector';
import { compareBy } from '@/utils/collections';

const readCookie = (name) => {
  const match = document.cookie.match(
    new RegExp('(^|;\\s*)(' + name + ')=([^;]*)')
  );
  return match ? decodeURIComponent(match[3]) : null;
};

export default {
  name: 'SbaNavbar',
  components: {
    NavbarLink,
    NavbarUserMenu,
    NavbarItems,
    NavbarItemLanguageSelector,
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
    };
  },
  data: () => ({
    showMenu: false,
    brand:
      '<img src="assets/img/icon-spring-boot-admin.svg"><span>Spring Boot Admin</span>',
    userName: '',
    csrfToken: null,
    csrfParameterName: null,
    currentLanguage: null,
  }),
  computed: {
    enabledViews() {
      return this.topLevelViews
        .filter(
          (view) =>
            view.handle &&
            (typeof view.isEnabled === 'undefined' || view.isEnabled())
        )
        .sort(compareBy((v) => v.order));
    },
    topLevelViews() {
      return this.views.filter((view) => !['instances'].includes(view.parent));
    },
    userSubMenuItems() {
      return this.enabledViews.filter((v) => v.parent === 'user');
    },
  },
  watch: {
    $route: function () {
      this.showMenu = false;
      this.showUserMenu = false;
    },
  },
  created() {
    this.brand = sbaConfig.uiSettings.brand;
    this.userName = sbaConfig.user ? sbaConfig.user.name : null;
    this.availableLocales = AVAILABLE_LANGUAGES;
    this.csrfToken = readCookie('XSRF-TOKEN');
    this.csrfParameterName = sbaConfig.csrf.parameterName;
  },
  mounted() {
    document.documentElement.classList.add('has-navbar-fixed-top');
  },
  beforeUnmount() {
    document.documentElement.classList.remove('has-navbar-fixed-top');
  },
  methods: {
    changeLocale(locale) {
      this.$i18n.locale = locale;
      moment.locale(this.$i18n.locale);
    },
  },
};
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

.navbar-top-menu {
  @apply flex items-baseline gap-2 text-white;
}
</style>
