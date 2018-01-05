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
    <sba-panel :title="`Datasource: ${upperFirst(dataSource)}`" v-if="current">
        <div slot="text">
            <div class="level datasource-current">
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading has-bullet has-bullet-info">Active connections</p>
                        <p v-text="current.active"></p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading">Min connections</p>
                        <p v-text="current.min"></p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading">Max connections</p>
                        <p v-if="current.max >= 0" v-text="current.max"></p>
                        <p v-else>unlimited</p>
                    </div>
                </div>
            </div>
            <datasource-chart :data="chartData"></datasource-chart>
        </div>
    </sba-panel>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import {Observable} from '@/utils/rxjs';
  import _ from 'lodash';
  import moment from 'moment';
  import datasourceChart from './datasource-chart';

  export default {
    props: ['instance', 'dataSource'],
    mixins: [subscribing],
    components: {
      datasourceChart
    },
    data: () => ({
      current: null,
      chartData: [],
    }),
    watch: {
      instance() {
        this.subscribe()
      },
      dataSource() {
        this.current = null;
        this.chartData = [];
      }
    },
    methods: {
      upperFirst: _.upperFirst,
      async fetchMetrics() {
        const responseActive = this.instance.fetchMetric('data.source.active.connections', {name: this.dataSource});
        const responseMin = this.instance.fetchMetric('data.source.min.connections', {name: this.dataSource});
        const responseMax = this.instance.fetchMetric('data.source.max.connections', {name: this.dataSource});

        return {
          active: (await responseActive).data.measurements[0].value,
          min: (await responseMin).data.measurements[0].value,
          max: (await responseMax).data.measurements[0].value
        };
      },
      createSubscription() {
        const vm = this;
        if (this.instance) {
          return Observable.timer(0, 2500)
            .concatMap(this.fetchMetrics)
            .subscribe({
              next: data => {
                vm.current = data;
                vm.chartData.push({...data, timestamp: moment.now().valueOf()});
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

<style lang="scss">
    .datasource-current {
        margin-bottom: 0 !important;
    }
</style>