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
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
            Fetching process metrics failed.
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <div class="level">
        <div class="level-item has-text-centered" v-if="pid">
          <div>
            <p class="heading">PID</p>
            <p v-text="pid" />
          </div>
        </div>
        <div class="level-item has-text-centered" v-if="uptime">
          <div>
            <p class="heading">Uptime</p>
            <p>
              <process-uptime :value="toMillis(uptime.value, uptime.baseUnit)" />
            </p>
          </div>
        </div>
        <div class="level-item has-text-centered" v-if="processCpuLoad">
          <div>
            <p class="heading">Process CPU Usage</p>
            <p v-text="processCpuLoad.toFixed(2)" />
          </div>
        </div>
        <div class="level-item has-text-centered" v-if="systemCpuLoad">
          <div>
            <p class="heading">System CPU Usage</p>
            <p v-text="systemCpuLoad.toFixed(2)" />
          </div>
        </div>
        <div class="level-item has-text-centered" v-if="systemCpuCount">
          <div>
            <p class="heading">CPUs</p>
            <p v-text="systemCpuCount" />
          </div>
        </div>
      </div>
    </div>
  </sba-panel>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import Instance from '@/services/instance';
  import {concatMap, timer} from '@/utils/rxjs';
  import {toMillis} from '../metrics/metric';
  import processUptime from './process-uptime';

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    mixins: [subscribing],
    // eslint-disable-next-line vue/no-unused-components
    components: {processUptime},
    data: () => ({
      hasLoaded: false,
      error: null,
      pid: null,
      uptime: {value: null, baseUnit: null},
      systemCpuLoad: null,
      processCpuLoad: null,
      systemCpuCount: null
    }),
    created() {
      this.fetchPid();
      this.fetchUptime();
      this.fetchCpuCount();
    },
    methods: {
      toMillis,
      async fetchUptime() {
        try {
          const response = await this.fetchMetric('process.uptime');
          this.uptime = {value: response.measurements[0].value, baseUnit: response.baseUnit};
        } catch (error) {
          this.error = error;
          console.warn('Fetching Uptime failed:', error);
        }
        this.hasLoaded = true;
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
      },
      async fetchCpuCount() {
        try {
          this.systemCpuCount = (await this.fetchMetric('system.cpu.count')).measurements[0].value;
        } catch (error) {
          console.warn('Fetching Cpu Count failed:', error);
        }
        this.hasLoaded = true;
      },
      createSubscription() {
        const vm = this;
        return timer(0, 2500)
          .pipe(concatMap(this.fetchCpuLoadMetrics))
          .subscribe({
            next: data => {
              vm.processCpuLoad = data.processCpuLoad;
              vm.systemCpuLoad = data.systemCpuLoad;
            },
            error: error => {
              vm.hasLoaded = true;
              console.warn('Fetching CPU Usage metrics failed:', error);
              vm.error = error;
            }
          });
      },
      async fetchCpuLoadMetrics() {
        const fetchProcessCpuLoad = this.fetchMetric('process.cpu.usage');
        const fetchSystemCpuLoad = this.fetchMetric('system.cpu.usage');
        let processCpuLoad;
        let systemCpuLoad;
        try {
          processCpuLoad = (await fetchProcessCpuLoad).measurements[0].value
        } catch (error) {
          console.warn('Fetching Process CPU Load failed:', error);
        }
        try {
          systemCpuLoad = (await fetchSystemCpuLoad).measurements[0].value
        } catch (error) {
          console.warn('Fetching Sytem CPU Load failed:', error);
        }
        return {
          processCpuLoad,
          systemCpuLoad
        };
      },
      async fetchMetric(name) {
        const response = await this.instance.fetchMetric(name);
        return response.data;
      }
    }
  }
</script>
