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
  <div class="instances">
    <div class="instances__body">
      <div class="instances__sidebar">
        <instance-sidebar
          v-if="instance"
          :views="views"
          :instance="instance"
          :application="application"
        />
      </div>
      <div class="instances__view">
        <router-view v-if="instance" :instance="instance" :application="application" />
      </div>
    </div>
  </div>
</template>

<script>
  import InstanceSidebar from './sidebar';

  export default {
    components: {InstanceSidebar},
    props: {
      instanceId: {
        type: String,
        required: true
      },
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
      instance() {
        return this.applications.findInstance(this.instanceId);
      },
      application() {
        return this.applications.findApplicationForInstance(this.instanceId);
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances',
        path: '/instances/:instanceId',
        component: this,
        props: true,
        isEnabled() {
          return false;
        }
      });
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  $sidebar-width-px: 220px;

  .instances {
    display: flex;
    flex-grow: 1;
    flex-direction: column;

    &__body {
      display: flex;
      flex-grow: 1;
    }

    &__view,
    &__sidebar {
      position: relative;
    }

    &__sidebar {
      z-index: 20;
      position: fixed;
      top: $navbar-height-px;
      bottom: 0;
      left: 0;
      width: $sidebar-width-px;
    }

    &__view {
      flex-grow: 1;
      flex-shrink: 1;
      z-index: 10;
      max-width: 100%;
      padding-left: $sidebar-width-px;
    }
  }
</style>
