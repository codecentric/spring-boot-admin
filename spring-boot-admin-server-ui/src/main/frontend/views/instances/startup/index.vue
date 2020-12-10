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
    <div v-if="error" class="message is-danger">
      <div class="message-body">
        <strong>
          <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
          <span v-text="$t('instances.startup.fetch_failed')" />
        </strong>
        <p v-text="error.message" />
      </div>
    </div>

    <tree-table v-if="hasLoaded" :tree="eventTree" />
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
      eventTree: null,
    }),
    created() {
      this.fetchStartup();
    },
    methods: {
      async fetchStartup() {
        this.error = null;
        try {
          const res = await this.instance.fetchStartup();
          this.eventTree = StartupActuatorService.parseAsTree(res.data)
        } catch (error) {
          console.warn('Fetching startup failed:', error);
          this.error = error;
        }
        this.hasLoaded = true;
      },
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

<style lang="scss">
  .startup-view {
  }
</style>
