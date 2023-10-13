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
  <sba-panel v-if="hasLoaded" :title="$t('instances.details.gc.title')">
    <sba-alert v-if="error" :error="error" :title="$t('term.fetch_failed')" />

    <div v-if="current" class="flex w-full">
      <div class="flex-1 text-center">
        <p class="font-bold" v-text="$t('instances.details.gc.count')" />
        <p v-text="current.count" />
      </div>
      <div class="flex-1 text-center">
        <p
          class="font-bold"
          v-text="$t('instances.details.gc.time_spent_total')"
        />
        <p v-text="`${current.total_time.asSeconds().toFixed(4)}s`" />
      </div>
      <div class="flex-1 text-center">
        <p
          class="font-bold"
          v-text="$t('instances.details.gc.time_spent_max')"
        />
        <p v-text="`${current.max.asSeconds().toFixed(4)}s`" />
      </div>
    </div>
  </sba-panel>
</template>

<script>
import moment from 'moment';
import { take } from 'rxjs/operators';

import subscribing from '@/mixins/subscribing';
import sbaConfig from '@/sba-config';
import Instance from '@/services/instance';
import { concatMap, delay, retryWhen, timer } from '@/utils/rxjs';
import { toMillis } from '@/views/instances/metrics/metric';

export default {
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    current: null,
  }),
  methods: {
    async fetchMetrics() {
      const response = await this.instance.fetchMetric('jvm.gc.pause');
      const measurements = response.data.measurements.reduce(
        (current, measurement) => ({
          ...current,
          [measurement.statistic.toLowerCase()]: measurement.value,
        }),
        {},
      );
      return {
        ...measurements,
        total_time: moment.duration(
          toMillis(measurements.total_time, response.baseUnit),
        ),
        max: moment.duration(toMillis(measurements.max, response.baseUnit)),
      };
    },
    createSubscription() {
      return timer(0, sbaConfig.uiSettings.pollTimer.gc)
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
          },
          error: (error) => {
            this.hasLoaded = true;
            console.warn('Fetching GC metrics failed:', error);
            this.error = error;
          },
        });
    },
  },
};
</script>
