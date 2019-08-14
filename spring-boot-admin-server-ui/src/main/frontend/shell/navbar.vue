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
  <nav id="navigation" class="navbar is-fixed-top">
    <div class="container">
      <div class="navbar-brand">
        <router-link class="navbar-item logo" to="/" v-html="brand" />

        <div class="navbar-burger burger" @click.stop="showMenu = !showMenu">
          <span />
          <span />
          <span />
        </div>
      </div>
      <div class="navbar-menu" :class="{'is-active' : showMenu}">
        <div class="navbar-end">
          <template v-for="view in enabledViews">
            <router-link
              v-if="view.name"
              :key="view.name"
              :to="{name: view.name}"
              class="navbar-item"
            >
              <component :is="view.handle" :applications="applications" :error="error" />
            </router-link>
            <a
              v-else
              :key="view.href"
              :href="view.href"
              class="navbar-item"
              target="_blank"
              rel="noopener noreferrer"
            >
              <component :is="view.handle" />
            </a>
          </template>

          <div class="navbar-item has-dropdown is-hoverable" v-if="userName">
            <a class="navbar-link">
              <font-awesome-icon icon="user-circle" size="lg" />&nbsp;<span v-text="userName" />
            </a>
            <div class="navbar-dropdown">
              <a class="navbar-item">
                <form action="logout" method="post">
                  <input v-if="csrfToken" type="hidden" :name="csrfParameterName" :value="csrfToken">
                  <button class="button is-icon" type="submit" value="logout">
                    <font-awesome-icon icon="sign-out-alt" />&nbsp;<span v-text="$t('navbar.logout')" />
                  </button>
                </form>
              </a>
            </div>
          </div>
          <div class="navbar-item has-dropdown is-hoverable">
            <a class="navbar-link">
              <span v-text="currentLanguage" />
            </a>
            <div class="navbar-dropdown">
              <a class="navbar-item" @click="changeLanguage(language)" v-for="language in availableLanguages" :key="language" v-text="language" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
  import sbaConfig from '@/sba-config'
  import {compareBy} from '@/utils/collections';
  import {AVAILABLE_LANGUAGES} from '@/i18n';
  import moment from 'moment';

  const readCookie = (name) => {
    const match = document.cookie.match(new RegExp('(^|;\\s*)(' + name + ')=([^;]*)'));
    return (match ? decodeURIComponent(match[3]) : null);
  };

  export default {
    data: () => ({
      showMenu: false,
      brand: '<img src="assets/img/icon-spring-boot-admin.svg"><span>Spring Boot Admin</span>',
      userName: null,
      csrfToken: null,
      csrfParameterName: null,
      availableLanguages: [],
      currentLanguage: null
    }),
    props: {
      views: {
        type: Array,
        default: () => []
      },
      applications: {
        type: Array,
        default: () => [],
      },
      error: {
        type: Error,
        default: null
      }
    },
    computed: {
      enabledViews() {
        return this.views.filter(
          view => view.handle && (typeof view.isEnabled === 'undefined' || view.isEnabled())
        ).sort(compareBy(v => v.order));
      }
    },
    methods: {
      changeLanguage(lang) {
        this.$i18n.locale = lang;
        this.currentLanguage = lang;
        moment.locale(lang);
      }
    },
    created() {
      this.brand = sbaConfig.uiSettings.brand;
      this.userName = sbaConfig.user ? sbaConfig.user.name : null;
      this.csrfToken = readCookie('XSRF-TOKEN');
      this.csrfParameterName = sbaConfig.csrf.parameterName;
      this.availableLanguages = AVAILABLE_LANGUAGES;
      this.currentLanguage = this.$i18n.locale;
    },
    mounted() {
      document.documentElement.classList.add('has-navbar-fixed-top');
    },
    beforeDestroy() {
      document.documentElement.classList.remove('has-navbar-fixed-top')
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .logo {
    align-self: center;
    flex-basis: 12em;
    flex-basis: content;
    max-height: 2.25em;
    padding: 0.5em 1em 0.5em 0.5em;
    font-size: 1.5em;
    font-weight: 600;
    white-space: nowrap;

    img {
      margin-right: 0.5em;
    }
  }
</style>
