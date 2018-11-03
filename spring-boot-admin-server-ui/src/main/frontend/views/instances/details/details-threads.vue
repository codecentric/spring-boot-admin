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
  <sba-panel title="Threads" v-if="hasLoaded">
    <div>
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
            Fetching threads metrics failed.
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <div class="level threads-current" v-if="current">
        <div class="level-item has-text-centered">
          <div>
            <p class="heading has-bullet has-bullet-warning">Live</p>
            <p v-text="current.live" />
          </div>
        </div>
        <div class="level-item has-text-centered">
          <div>
            <p class="heading  has-bullet has-bullet-info">Daemon</p>
            <p v-text="current.daemon" />
          </div>
        </div>
        <div class="level-item has-text-centered">
          <div>
            <p class="heading">Peak Live</p>
            <p v-text="current.peak" />
          </div>
        </div>
      </div>
      <threads-chart v-if="chartData.length > 0" :data="chartData" />
    </div>
  </sba-panel>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import Instance from '@/services/instance';
  import {concatMap, timer} from '@/utils/rxjs';
  import moment from 'moment';
  import threadsChart from './threads-chart';

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    mixins: [subscribing],
    // eslint-disable-next-line vue/no-unused-components
    components: {threadsChart},
    data: () => ({
      hasLoaded: false,
      error: null,
      current: null,
      chartData: [],
    }),
    methods: {
      async fetchMetrics() {
        const responseLive = this.instance.fetchMetric('jvm.threads.live');
        const responsePeak = this.instance.fetchMetric('jvm.threads.peak');
        const responseDaemon = this.instance.fetchMetric('jvm.threads.daemon');

        return {
          live: (await responseLive).data.measurements[0].value,
          peak: (await responsePeak).data.measurements[0].value,
          daemon: (await responseDaemon).data.measurements[0].value
        };
      },
      createSubscription() {
        const vm = this;
        return timer(0, 2500)
          .pipe(concatMap(this.fetchMetrics))
          .subscribe({
            next: data => {
              vm.hasLoaded = true;
              vm.current = data;
              vm.chartData.push({...data, timestamp: moment().valueOf()});
            },
            error: error => {
              vm.hasLoaded = true;
              console.warn('Fetching threads metrics failed:', error);
              vm.error = error;
            }
          });
      }
    }
  }
</script>

<style lang="scss">
  .threads-current {
    margin-bottom: 0 !important;
  }
</style>
