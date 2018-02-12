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
    <sba-panel :title="`Cache: ${cacheName}`" v-if="hasLoaded">
        <div>
            <div v-if="error" class="message is-danger">
                <div class="message-body">
                    <strong>
                        <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"></font-awesome-icon>
                        Fetching cache metrics failed.
                    </strong>
                </div>
            </div>
            <div class="level cache-current" v-if="current">
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading has-bullet has-bullet-info">Hits</p>
                        <p v-text="current.hit"></p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading has-bullet has-bullet-warning">Total</p>
                        <p v-text="current.total"></p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading">Hit ratio</p>
                        <p v-text="ratio"></p>
                    </div>
                </div>
            </div>
            <cache-chart v-if="chartData.length > 0" :data="chartData"></cache-chart>
        </div>
    </sba-panel>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import {Observable} from '@/utils/rxjs';
  import moment from 'moment';
  import cacheChart from './cache-chart';

  export default {
    props: ['instance', 'cacheName'],
    mixins: [subscribing],
    components: {cacheChart},
    data: () => ({
      hasLoaded: false,
      error: null,
      current: null,
      chartData: [],
    }),
    computed: {
      ratio() {
        if (this.current.total > 0) {
          return (this.current.hit / this.current.total * 100).toFixed(2) + '%';
        }
        return '-';
      }
    },
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
      async fetchMetrics() {
        const responseHit = this.instance.fetchMetric('cache.requests', {name: this.cacheName, result: 'hit'});
        const responseMiss = this.instance.fetchMetric('cache.requests', {name: this.cacheName, result: 'miss'});
        const hit = (await responseHit).data.measurements[0].value;
        const miss = (await responseMiss).data.measurements[0].value;
        return {
          hit,
          miss,
          total: hit + miss
        };
      },
      createSubscription() {
        const vm = this;
        if (this.instance) {
          return Observable.timer(0, 2500)
            .concatMap(vm.fetchMetrics)
            .subscribe({
              next: data => {
                vm.current = data;
                vm.chartData.push({...data, timestamp: moment.now().valueOf()});
              },
              error: error => {
                vm.hasLoaded = true;
                console.warn(`Fetching cache ${vm.cacheName} metrics failed:`, error);
                vm.error = error;
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