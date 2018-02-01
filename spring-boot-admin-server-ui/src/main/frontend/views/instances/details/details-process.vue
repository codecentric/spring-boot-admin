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
        <div slot="text">
            <div v-if="error" class="message is-danger">
                <div class="message-body">
                    <strong>
                        <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"></font-awesome-icon>
                        Fetching process metrics failed.
                    </strong>
                </div>
            </div>
            <div class="level">
                <div class="level-item has-text-centered" v-if="pid">
                    <div>
                        <p class="heading">PID</p>
                        <p v-text="pid"></p>
                    </div>
                </div>
                <div class="level-item has-text-centered" v-if="metrics['process.uptime']">
                    <div>
                        <p class="heading">Uptime</p>
                        <p>
                            <process-uptime :value="metrics['process.uptime']"></process-uptime>
                        </p>
                    </div>
                </div>
                <div class="level-item has-text-centered" v-if="metrics['system.cpu.count']">
                    <div>
                        <p class="heading">CPUs</p>
                        <p v-text="metrics['system.cpu.count']"></p>
                    </div>
                </div>
                <div class="level-item has-text-centered" v-if="metrics['system.load.average.1m']">
                    <div>
                        <p class="heading">System Load (last 1m)</p>
                        <p v-text="metrics['system.load.average.1m'].toFixed(2)"></p>
                    </div>
                </div>
            </div>
        </div>
    </sba-panel>
</template>

<script>
  import _ from 'lodash';
  import processUptime from './process-uptime'

  export default {
    props: ['instance'],
    components: {processUptime},
    data: () => ({
      hasLoaded: false,
      error: null,
      pid: null,
      metrics: {
        'process.uptime': null,
        'system.load.average.1m': null,
        'system.cpu.count': null,
      }
    }),
    created() {
      this.fetchMetrics();
      this.fetchPid();
    },
    watch: {
      instance() {
        this.fetchMetrics();
        this.fetchPid();
      }
    },
    methods: {
      async fetchMetrics() {
        if (this.instance) {
          const vm = this;
          vm.error = null;
          try {
            await Promise.all(_.entries(vm.metrics)
              .map(async ([name]) => {
                  const response = await vm.instance.fetchMetric(name);
                  vm.metrics[name] = response.data.measurements[0].value;
                }
              ));
          } catch (error) {
            console.warn('Fetching process metrics failed:', error);
            this.error = error;
          }
          this.hasLoaded = true;
        }
      },
      async fetchPid() {
        if (this.instance && this.instance.hasEndpoint('env')) {
          try {
            const response = await this.instance.fetchEnv('PID');
            this.pid = response.data.property.value;
          } catch (error) {
            console.warn('Fetching PID failed:', error);
          }
        }
      }
    }
  }
</script>