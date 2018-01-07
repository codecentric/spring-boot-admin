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
    <sba-panel title="Garbage Collection Pauses" v-if="current">
        <div slot="text">
            <div class="level">
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading">Count</p>
                        <p v-text="current.count"></p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading">Total time spent</p>
                        <p v-text="`${current.totalTime.asSeconds()}s`"></p>
                    </div>
                </div>
            </div>
        </div>
    </sba-panel>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import {Observable} from '@/utils/rxjs';
  import _ from 'lodash';
  import moment from 'moment';

  export default {
    props: ['instance'],
    mixins: [subscribing],
    data: () => ({
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
            [_.lowerFirst(measurement.statistic)]: measurement.value
          }), {}
        );
        return {...measurements, totalTime: moment.duration(Math.round(measurements.totalTime / 1000))};
      },
      createSubscription() {
        const vm = this;
        if (this.instance) {
          return Observable.timer(0, 2500)
            .concatMap(this.fetchMetrics)
            .subscribe({
              next: data => {
                vm.current = data;
              },
              errors: err => {
                vm.unsubscribe();
              }
            });
        }
      }
    }
  }
</script>