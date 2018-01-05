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
    <sba-panel title="Process" v-if="pid || metrics['process.uptime'] ">
        <div class="level" slot="text">
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
    </sba-panel>
</template>

<script>
  import _ from 'lodash';
  import processUptime from './process-uptime'

  export default {
    props: ['instance'],
    components: {
      processUptime
    },
    data: () => ({
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
          _.entries(vm.metrics).forEach(async ([name]) => {
              const response = await vm.instance.fetchMetric(name);
              vm.metrics[name] = response.data.measurements[0].value;
            }
          )
        }
      },
      async fetchPid() {
        if (this.instance) {
          const vm = this;
          const response = await vm.instance.fetchEnv('PID');
          vm.pid = response.data.property.value;
        }
      }
    }
  }
</script>