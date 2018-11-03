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
  <div id="app">
    <sba-navbar :views="mainViews" :applications="applications" :error="error" />
    <router-view :views="childViews" :applications="applications" :error="error" />
  </div>
</template>

<script>
  import sbaNavbar from './navbar';

  export default {
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
    // eslint-disable-next-line vue/no-unused-components
    components: {sbaNavbar},
    computed: {
      mainViews() {
        return this.views.filter(view => !view.parent);
      },
      activeMainViewName() {
        const currentView = this.$route.meta.view;
        return currentView && (currentView.parent || currentView.name);
      },
      childViews() {
        return this.views.filter(view => view.parent === this.activeMainViewName);
      }
    }
  }
</script>
