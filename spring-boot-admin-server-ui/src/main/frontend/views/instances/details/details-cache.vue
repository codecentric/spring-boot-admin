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
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
            Fetching cache metrics failed.
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <div class="level cache-current" v-if="current">
        <div class="level-item has-text-centered">
          <div>
            <p class="heading has-bullet has-bullet-info">Hits</p>
            <p v-text="current.hit" />
          </div>
        </div>
        <div class="level-item has-text-centered">
          <div>
            <p class="heading has-bullet has-bullet-warning">Misses</p>
            <p v-text="current.miss" />
          </div>
        </div>
        <div class="level-item has-text-centered">
          <div>
            <p class="heading">Hit ratio</p>
            <p v-text="ratio" />
          </div>
        </div>
        <div v-if="current.size" class="level-item has-text-centered">
          <div>
            <p class="heading">Size</p>
            <p v-text="current.size" />
          </div>
        </div>
      </div>
      <cache-chart v-if="chartData.length > 0" :data="chartData" />
    </div>
  </sba-panel>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import Instance from '@/services/instance';
  import {concatMap, timer} from '@/utils/rxjs';
  import moment from 'moment';
  import cacheChart from './cache-chart';

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      },
      cacheName: {
        type: String,
        required: true
      }
    },
    mixins: [subscribing],
    // eslint-disable-next-line vue/no-unused-components
    components: {cacheChart},
    data: () => ({
      hasLoaded: false,
      error: null,
      current: null,
      disableSize: false,
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
    methods: {
      async fetchMetrics() {
        const responseHit = this.instance.fetchMetric('cache.gets', {name: this.cacheName, result: 'hit'});
        const responseMiss = this.instance.fetchMetric('cache.gets', {name: this.cacheName, result: 'miss'});
        let size = undefined;
        if (!this.disableSize) {
          const responsSize = this.instance.fetchMetric('cache.size', {name: this.cacheName});
          try {
            size = (await responsSize).data.measurements[0].value;
          } catch (error) {
            this.disableSize = true;
            console.warn('Fetching cache size failed - error is ignored', error)
          }
        }
        const hit = (await responseHit).data.measurements[0].value;
        const miss = (await responseMiss).data.measurements[0].value;
        return {
          hit,
          miss,
          total: hit + miss,
          size
        };
      },
      createSubscription() {
        const vm = this;
        return timer(0, 2500)
          .pipe(concatMap(vm.fetchMetrics))
          .subscribe({
            next: data => {
              vm.hasLoaded = true;
              vm.current = data;
              vm.chartData.push({...data, timestamp: moment().valueOf()});
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
</script>

<style lang="scss">
  .datasource-current {
    margin-bottom: 0 !important;
  }
</style>
