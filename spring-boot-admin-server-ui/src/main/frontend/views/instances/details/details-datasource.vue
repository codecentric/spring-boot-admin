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
  <sba-panel
    v-if="hasLoaded"
    :title="
      $t('instances.details.datasource.title', { dataSource: dataSource })
    "
  >
    <sba-alert v-if="error" :error="error" :title="$t('term.fetch_failed')" />

    <dl
      v-if="current"
      class="px-4 py-3 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
    >
      <dt
        class="text-sm font-medium text-gray-500 sm:col-span-4"
        v-text="$t('instances.details.datasource.active_connections')"
      />
      <dd
        class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2"
        v-text="current.active"
      />

      <dt
        class="text-sm font-medium text-gray-500 sm:col-span-4"
        v-text="$t('instances.details.datasource.min_connections')"
      />
      <dd
        class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2"
        v-text="current.min"
      />

      <dt
        class="text-sm font-medium text-gray-500 sm:col-span-4"
        v-text="$t('instances.details.datasource.max_connections')"
      />
      <dd
        class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2"
        v-if="current.max >= 0"
        v-text="current.max"
      />
      <dd
        class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2"
        v-else
        v-text="$t('instances.details.datasource.unlimited')"
      />
    </dl>
    <datasource-chart v-if="chartData.length > 0" :data="chartData" />
  </sba-panel>
</template>

<script>
import moment from 'moment';
import { concatMap, delay, retryWhen, timer } from 'rxjs';
import { take } from 'rxjs/operators';

import subscribing from '@/mixins/subscribing';
import sbaConfig from '@/sba-config';
import Instance from '@/services/instance';
import datasourceChart from '@/views/instances/details/datasource-chart';

export default {
  components: { datasourceChart },
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
    dataSource: {
      type: String,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    current: null,
    chartData: [],
  }),
  methods: {
    async fetchMetrics() {
      const responseActive = this.instance.fetchMetric(
        'jdbc.connections.active',
        { name: this.dataSource },
      );
      const responseMin = this.instance.fetchMetric('jdbc.connections.min', {
        name: this.dataSource,
      });
      const responseMax = this.instance.fetchMetric('jdbc.connections.max', {
        name: this.dataSource,
      });

      return {
        active: (await responseActive).data.measurements[0].value,
        min: (await responseMin).data.measurements[0].value,
        max: (await responseMax).data.measurements[0].value,
      };
    },
    createSubscription() {
      return timer(0, sbaConfig.uiSettings.pollTimer.datasource)
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
              `Fetching datasource ${this.dataSource} metrics failed:`,
              error,
            );
            this.error = error;
          },
        });
    },
  },
};
</script>

<style lang="css">
.datasource-current {
  margin-bottom: 0 !important;
}
</style>
