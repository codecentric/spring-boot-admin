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
  <sba-panel title="Process" v-if="hasLoaded">
    <div>
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"/>
            Fetching process metrics failed.
          </strong>
          <p v-text="error.message"/>
        </div>
      </div>
      <div class="level">
        <div class="level-item has-text-centered" v-if="pid">
          <div>
            <p class="heading">PID</p>
            <p v-text="pid"/>
          </div>
        </div>
        <div class="level-item has-text-centered" v-if="uptime">
          <div>
            <p class="heading">Uptime</p>
            <p>
              <process-uptime :value="uptime"/>
            </p>
          </div>
        </div>
        <div class="level-item has-text-centered" v-if="cpuCount">
          <div>
            <p class="heading">CPUs</p>
            <p v-text="cpuCount"/>
          </div>
        </div>
        <div class="level-item has-text-centered" v-if="systemLoad">
          <div>
            <p class="heading">System Load (last 1m)</p>
            <p v-text="systemLoad.toFixed(2)"/>
          </div>
        </div>
      </div>
    </div>
  </sba-panel>
</template>

<script>
  import Instance from '@/services/instance';
  import processUptime from './process-uptime';

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    components: {processUptime},
    data: () => ({
      hasLoaded: false,
      error: null,
      pid: null,
      uptime: null,
      systemLoad: null,
      cpuCount: null
    }),
    created() {
      this.fetchUptime();
      this.fetchCpuCount();
      this.fetchSystemLoad();
      this.fetchPid();
    },
    methods: {
      async fetchUptime() {
        try {
          this.uptime = await this.fetchMetric('process.uptime');
        } catch (error) {
          this.error = error;
          console.warn('Fetching Uptime failed:', error);
        }
        this.hasLoaded = true;
      },
      async fetchSystemLoad() {
        try {
          this.systemLoad = await this.fetchMetric('system.load.average.1m');
        } catch (error) {
          console.warn('Fetching System Load failed:', error);
        }
        this.hasLoaded = true;
      },
      async fetchCpuCount() {
        try {
          this.cpuCount = await this.fetchMetric('system.cpu.count');
        } catch (error) {
          console.warn('Fetching Cpu Count failed:', error);
        }
        this.hasLoaded = true;
      },
      async fetchMetric(name) {
        const response = await this.instance.fetchMetric(name);
        return response.data.measurements[0].value;
      },
      async fetchPid() {
        if (this.instance.hasEndpoint('env')) {
          try {
            const response = await this.instance.fetchEnv('PID');
            this.pid = response.data.property.value;
          } catch (error) {
            console.warn('Fetching PID failed:', error);
          }
          this.hasLoaded = true;
        }
      }
    }
  }
</script>
