<!--
  - Copyright 2014-2019 the original author or authors.
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
  <sba-panel v-if="hasLoaded" :title="`Cache: ${cacheName}`">
    <sba-alert v-if="error" :error="error" :title="$t('term.fetch_failed')" />
    <dl
      v-if="current"
      class="px-4 py-3 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
    >
      <template v-if="current.hit !== undefined">
        <dt
          class="text-sm font-medium text-gray-500 sm:col-span-4"
          v-text="$t('instances.details.cache.hits')"
        />
        <dd
          class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2"
          v-text="current.hit"
        />
      </template>
      <template v-if="current.miss !== undefined">
        <dt
          class="text-sm font-medium text-gray-500 sm:col-span-4"
          v-text="$t('instances.details.cache.misses')"
        />
        <dd
          class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2"
          v-text="current.miss"
        />
      </template>
      <template v-if="ratio !== undefined">
        <dt
          class="sm:col-span-4"
          v-text="$t('instances.details.cache.hit_ratio')"
        />
        <dd v-text="ratio" />
      </template>
      <template v-if="current.size !== undefined">
        <dt class="sm:col-span-4" v-text="$t('instances.details.cache.size')" />
        <dd v-text="current.size" />
      </template>
    </dl>
    <cache-chart v-if="chartData.length > 0" :data="chartData" />
  </sba-panel>
</template>

<script>
import moment from 'moment';
import { take } from 'rxjs/operators';

import subscribing from '@/mixins/subscribing';
import sbaConfig from '@/sba-config';
import Instance from '@/services/instance';
import { concatMap, delay, retryWhen, timer } from '@/utils/rxjs';
import cacheChart from '@/views/instances/details/cache-chart';

export default {
  components: { cacheChart },
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
    cacheName: {
      type: String,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    current: null,
    shouldFetchCacheSize: true,
    shouldFetchCacheHits: true,
    shouldFetchCacheMisses: true,
    chartData: [],
  }),
  computed: {
    ratio() {
      if (Number.isFinite(this.current.hit) && Number.isFinite(this.current)) {
        const total = this.current.hit + this.current.miss;
        return total > 0
          ? ((this.current.hit / total) * 100).toFixed(2) + '%'
          : '-';
      }
      return undefined;
    },
  },
  methods: {
    async fetchMetrics() {
      const [hit, miss, size] = await Promise.all([
        this.fetchCacheHits(),
        this.fetchCacheMisses(),
        this.fetchCacheSize(),
      ]);
      return {
        hit: hit,
        miss: miss,
        total: hit + (miss || 0),
        size,
      };
    },
    async fetchCacheHits() {
      if (this.shouldFetchCacheHits) {
        try {
          const response = await this.instance.fetchMetric('cache.gets', {
            name: this.cacheName,
            result: 'hit',
          });
          return response.data.measurements[0].value;
        } catch (error) {
          this.shouldFetchCacheHits = false;
          console.warn(
            `Fetching cache ${this.cacheName} hits failed - error is ignored`,
            error,
          );
          return undefined;
        }
      }
    },
    async fetchCacheMisses() {
      if (this.shouldFetchCacheMisses) {
        try {
          const response = await this.instance.fetchMetric('cache.gets', {
            name: this.cacheName,
            result: 'miss',
          });
          return response.data.measurements[0].value;
        } catch (error) {
          this.shouldFetchCacheMisses = false;
          console.warn(
            `Fetching cache ${this.cacheName} misses failed - error is ignored`,
            error,
          );
          return undefined;
        }
      }
    },
    async fetchCacheSize() {
      if (this.shouldFetchCacheSize) {
        try {
          const response = await this.instance.fetchMetric('cache.size', {
            name: this.cacheName,
          });
          return response.data.measurements[0].value;
        } catch (error) {
          this.shouldFetchCacheSize = false;
          console.warn(
            `Fetching cache ${this.cacheName} size failed - error is ignored`,
            error,
          );
          return undefined;
        }
      }
    },
    createSubscription() {
      return timer(0, sbaConfig.uiSettings.pollTimer.cache)
        .pipe(
          concatMap(this.fetchMetrics),
          retryWhen((err) => {
            return err.pipe(delay(1000), take(5));
          }),
        )
        .subscribe({
          next: (data) => {
            this.hasLoaded = true;
            this.current = data;
            this.chartData.push({ ...data, timestamp: moment().valueOf() });
          },
          error: (error) => {
            this.hasLoaded = true;
            console.warn(
              `Fetching cache ${this.cacheName} metrics failed:`,
              error,
            );
            this.error = error;
          },
        });
    },
  },
};
</script>
