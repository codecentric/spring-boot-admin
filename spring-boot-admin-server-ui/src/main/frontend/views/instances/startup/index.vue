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
  <div class="section startup-view">
    <sba-alert v-if="error" :error="error" :title="$t('instances.startup.fetch_failed')" />

    <tree-table v-if="hasLoaded" :expand="expandedNodes" :tree="eventTree" @change="saveTreeState" />
  </div>
</template>

<script>
import Instance from '@/services/instance';
import {StartupActuatorService} from '@/services/startup-actuator';
import {VIEW_GROUP} from '../../index';
import TreeTable from '@/views/instances/startup/tree-table';

export default {
  components: {TreeTable},
  props: {
    instance: {
      type: Instance,
      required: true
    }
  },
  data: () => ({
    error: null,
    hasLoaded: false,
    expandedNodes: null,
    eventTree: null,
  }),
  async created() {
    await this.fetchStartup();

    this.loadTreeState();
    this.expandEventId();

    this.hasLoaded = true;
  },
  methods: {
    async fetchStartup() {
      this.error = null;
      try {
        const res = await this.instance.fetchStartup();
        this.eventTree = StartupActuatorService.parseAsTree(res.data);
      } catch (error) {
        console.warn('Fetching startup failed:', error);
        this.error = error;
      }
    },
    expandEventId() {
      let queryParams = this.$router.currentRoute.query;
      if (queryParams && queryParams.id) {
        this.expandedNodes = new Set(this.eventTree.getPath(+queryParams.id));
      }
    },
    loadTreeState() {
      if (window.localStorage) {
        let value = localStorage.getItem(`applications/${this.instance.registration.name}/startup`);
        if (value) {
          let parse = JSON.parse(value);
          this.expandedNodes = new Set(parse.expandedNodes);
        }
      }
    },
    saveTreeState($event) {
      if (window.localStorage) {
        localStorage.setItem(`applications/${this.instance.registration.name}/startup`, JSON.stringify({
          expandedNodes: [...$event.expandedNodes]
        }));
      }
    }
  },
  install({viewRegistry}) {
    viewRegistry.addView({
      name: 'instances/startup',
      parent: 'instances',
      path: 'startup',
      component: this,
      label: 'instances.startup.label',
      group: VIEW_GROUP.LOGGING,
      order: 600,
      isEnabled: ({instance}) => instance.hasEndpoint('startup')
    });
  }
}
</script>
