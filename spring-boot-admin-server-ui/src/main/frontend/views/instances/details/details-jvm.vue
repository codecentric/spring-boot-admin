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
    <sba-panel title="JVM" v-if="metrics['process.uptime']">
        <div class="content" slot="text">
            <table class="table data-table">
                <tr>
                    <td>Uptime</td>
                    <td colspan="2">
                        <jvm-uptime :value="metrics['process.uptime']"></jvm-uptime>
                    </td>
                </tr>
                <tr v-if="metrics['system.load.average.1m']">
                    <td>System load (last 1m)</td>
                    <td colspan="2"
                        v-text="metrics['system.load.average.1m'].toFixed(2)"></td>
                </tr>
                <tr>
                    <td>Available CPUs</td>
                    <td colspan="2" v-text="metrics['system.cpu.count']"></td>
                </tr>
                <tr>
                    <td rowspan="3">Threads</td>
                    <td>live</td>
                    <td v-text="metrics['jvm.threads.live']"></td>
                </tr>
                <tr>
                    <td>peak</td>
                    <td v-text="metrics['jvm.threads.peak']"></td>
                </tr>
                <tr>
                    <td>daemon</td>
                    <td v-text="metrics['jvm.threads.daemon']"></td>
                </tr>
            </table>
        </div>
    </sba-panel>
</template>

<script>
  import _ from 'lodash';
  import jvmUptime from './jvm-uptime'

  export default {
    props: ['instance'],
    components: {
      jvmUptime
    },
    data: () => ({
      metrics: {
        'process.uptime': null,
        'system.load.average.1m': null,
        'system.cpu.count': null,
        'jvm.threads.live': null,
        'jvm.threads.peak': null,
        'jvm.threads.daemon': null
      }
    }),
    created() {
      this.fetchMetrics();
    },
    watch: {
      instance(newVal, oldVal) {
        if (newVal !== oldVal) {
          this.fetchMetrics();
        }
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
      }
    }
  }
</script>