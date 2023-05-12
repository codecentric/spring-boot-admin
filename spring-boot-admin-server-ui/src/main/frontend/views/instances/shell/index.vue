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
  <div class="h-full">
    <sba-wave />
    <div class="h-full">
      <Sidebar
        v-if="instance"
        :key="instanceId"
        :application="application"
        :instance="instance"
        :views="sidebarViews"
      />
      <main class="min-h-full relative z-0 ml-10 md:ml-60 transition-all">
        <router-view
          v-if="instance"
          :application="application"
          :instance="instance"
        />
      </main>
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue';

import { useViewRegistry } from '@/composables/ViewRegistry';
import { useApplicationStore } from '@/composables/useApplicationStore';
import { findApplicationForInstance, findInstance } from '@/store';
import Sidebar from '@/views/instances/shell/sidebar';

export default defineComponent({
  components: {
    Sidebar,
  },
  setup() {
    const { applications } = useApplicationStore();
    const { views } = useViewRegistry();
    return {
      views,
      applications,
    };
  },
  data() {
    return {
      instanceId: this.$route.params.instanceId,
      background: {},
    };
  },
  computed: {
    sidebarViews() {
      return this.views.filter((v) => v.parent === this.activeMainViewName);
    },
    instance() {
      return findInstance(this.applications, this.instanceId);
    },
    application() {
      return findApplicationForInstance(this.applications, this.instanceId);
    },
    activeMainViewName() {
      const currentView = this.$route.meta.view;
      return currentView && (currentView.parent || currentView.name);
    },
  },
  watch: {
    $route: {
      immediate: true,
      handler() {
        this.instanceId = this.$route.params.instanceId;
      },
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances',
      path: '/instances/:instanceId',
      component: this,
      props: true,
      isEnabled() {
        return false;
      },
    });
  },
});
</script>
