<!--
  - Copyright 2014-2018 the original author or authors.
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
        <router-link class="navbar-item logo" to="/" v-html="brand"/>

        <div class="navbar-burger burger" @click.stop="showMenu = !showMenu">
          <span/>
          <span/>
          <span/>
        </div>
      </div>
      <div class="navbar-menu" :class="{'is-active' : showMenu}">
        <div class="navbar-start"/>
        <div class="navbar-end">
          <router-link class="navbar-item" v-for="view in enabledViews" :to="{name: view.name}" :key="view.name">
            <component :is="view.label" :applications="applications" :error="error"/>
          </router-link>

          <div class="navbar-item has-dropdown is-hoverable" v-if="userName">
            <a class="navbar-link">
              <font-awesome-icon icon="user-circle" size="lg"/>&nbsp;<span v-text="userName"/>
            </a>
            <div class="navbar-dropdown">
              <a class="navbar-item">
                <form action="logout" method="post">
                  <button class="button is-icon" type="submit" value="logout">
                    <font-awesome-icon icon="sign-out-alt"/>&nbsp;Log out
                  </button>
                </form>
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
  import {compareBy} from '@/utils/collections';

  export default {
    data: () => ({
      showMenu: false,
      brand: '<img src="assets/img/icon-spring-boot-admin.svg"><span>Spring Boot Admin</span>',
      userName: null
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
        type: null,
        default: null
      }
    },
    computed: {
      enabledViews() {
        return [...this.views].filter(
          view => view.label && (typeof view.isEnabled === 'undefined' || view.isEnabled())
        ).sort(compareBy(v => v.order));
      }
    },
    created() {
      /* global SBA */
      if (SBA) {
        if (SBA.uiSettings) {
          this.brand = SBA.uiSettings.brand || this.brand;
        }

        if (SBA.user) {
          this.userName = SBA.user.name;
        }
      }
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
    font-size: 1.5rem;
    font-weight: 600;
    white-space: nowrap;
    padding: 0;

    & span {
      margin: 0.5rem 1rem 0.5rem 0.5rem;
    }
    & img {
      max-height: 2.25rem;
    }
  }
</style>
