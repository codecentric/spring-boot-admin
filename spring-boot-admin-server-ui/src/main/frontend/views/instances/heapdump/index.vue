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
  <sba-instance-section>
    <template #before>
      <sba-sticky-subnav>
        <div class="text-right">
          <sba-button @click="downloadHeap()">
            <font-awesome-icon icon="download" />&nbsp;
            <span v-text="$t('instances.heapdump.download')" />
          </sba-button>
        </div>
      </sba-sticky-subnav>
    </template>
    <div
      class="m-auto flex relative items-center gap-2 py-3 pl-4 pr-10 leading-normal text-red-700 bg-red-100 rounded-lg shadow mb-3 backdrop-filter backdrop-blur-sm bg-opacity-80"
      role="alert"
    >
      <div>
        <svg
          class="h-12"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </div>
      <div class="flex-1">
        <p v-text="$t('instances.heapdump.warn_sensitive_data')" />
        <p v-text="$t('instances.heapdump.warn_dump_expensive')" />
      </div>
    </div>
  </sba-instance-section>
</template>

<script>
import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

export default {
  components: { SbaInstanceSection },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  methods: {
    downloadHeap() {
      window.open(`instances/${this.instance.id}/actuator/heapdump`, '_blank');
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/heapdump',
      parent: 'instances',
      path: 'heapdump',
      component: this,
      label: 'instances.heapdump.label',
      group: VIEW_GROUP.JVM,
      order: 800,
      isEnabled: ({ instance }) => instance.hasEndpoint('heapdump'),
    });
  },
};
</script>

<style lang="css">
.heapdump {
  display: flex;
  justify-content: space-around;
}
.heapdump > div {
  display: flex;
  flex-direction: column;
}
</style>
