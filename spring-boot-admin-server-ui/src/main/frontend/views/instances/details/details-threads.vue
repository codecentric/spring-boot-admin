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
    <sba-panel title="Threads" v-if="metrics['jvm.threads.live']">
        <div class="level" slot="text">
            <div class="level-item has-text-centered">
                <div>
                    <p class="heading">Live</p>
                    <p v-text="metrics['jvm.threads.live']"></p>
                </div>
            </div>
            <div class="level-item has-text-centered">
                <div>
                    <p class="heading">Live Peak</p>
                    <p v-text="metrics['jvm.threads.peak']"></p>
                </div>
            </div>
            <div class="level-item has-text-centered">
                <div>
                    <p class="heading">Daemon</p>
                    <p v-text="metrics['jvm.threads.daemon']"></p>
                </div>
            </div>
        </div>
    </sba-panel>
</template>

<script>
  import _ from 'lodash';

  export default {
    props: ['instance'],
    data: () => ({
      metrics: {
        'jvm.threads.live': null,
        'jvm.threads.peak': null,
        'jvm.threads.daemon': null
      }
    }),
    created() {
      this.fetchMetrics();
    },
    watch: {
      instance() {
        this.fetchMetrics();
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