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
    <sba-panel title="Garbage Collection Pauses" v-if="hasLoaded">
        <div>
            <div v-if="error" class="message is-danger">
                <div class="message-body">
                    <strong>
                        <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"></font-awesome-icon>
                        Fetching GC metrics failed.
                    </strong>
                    <p v-text="error.message"></p>
                </div>
            </div>
            <div class="level" v-if="current">
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading">Count</p>
                        <p v-text="current.count"></p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading">Total time spent</p>
                        <p v-text="`${current.total_time.asSeconds()}s`"></p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading">Max time spent</p>
                        <p v-text="`${current.max.asSeconds()}s`"></p>
                    </div>
                </div>
            </div>
        </div>
    </sba-panel>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import {Observable} from '@/utils/rxjs';
  import moment from 'moment';

  export default {
    props: ['instance'],
    mixins: [subscribing],
    data: () => ({
      hasLoaded: false,
      error: null,
      current: null,
    }),
    watch: {
      instance() {
        this.subscribe()
      },
      dataSource() {
        this.current = null;
      }
    },
    methods: {
      async fetchMetrics() {
        const response = await this.instance.fetchMetric('jvm.gc.pause');
        const measurements = response.data.measurements.reduce(
          (current, measurement) => ({
            ...current,
            [measurement.statistic.toLowerCase()]: measurement.value
          }), {}
        );
        return {
          ...measurements,
          total_time: moment.duration(Math.round(measurements.total_time * 1000)),
          max: moment.duration(Math.round(measurements.max * 1000)),
        };
      },
      createSubscription() {
        const vm = this;
        if (this.instance) {
          return Observable.timer(0, 2500)
            .concatMap(this.fetchMetrics)
            .subscribe({
              next: data => {
                vm.hasLoaded = true;
                vm.current = data;
              },
              error: error => {
                vm.hasLoaded = true;
                console.warn('Fetching GC metrics failed:', error);
                vm.error = error;
              }
            });
        }
      }
    }
  }
</script>