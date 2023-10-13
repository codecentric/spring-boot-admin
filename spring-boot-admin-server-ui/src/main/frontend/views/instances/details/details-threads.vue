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
  <sba-panel v-if="hasLoaded" :title="$t('instances.details.threads.title')">
    <div>
      <sba-alert v-if="error" :error="error" :title="$t('term.fetch_failed')" />

      <div v-if="current" class="flex w-full">
        <div class="flex-1 text-center">
          <p class="font-bold" v-text="$t('instances.details.threads.live')" />
          <p v-text="current.live" />
        </div>
        <div class="flex-1 text-center">
          <p
            class="font-bold"
            v-text="$t('instances.details.threads.daemon')"
          />
          <p v-text="current.daemon" />
        </div>
        <div class="flex-1 text-center">
          <p
            class="font-bold"
            v-text="$t('instances.details.threads.peak_live')"
          />
          <p v-text="current.peak" />
        </div>
      </div>

      <threads-chart v-if="chartData.length > 0" :data="chartData" />
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
import threadsChart from '@/views/instances/details/threads-chart';

export default {
  components: { threadsChart },
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
        daemon: (await responseDaemon).data.measurements[0].value,
      };
    },
    createSubscription() {
      return timer(0, sbaConfig.uiSettings.pollTimer.threads)
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
            console.warn('Fetching threads metrics failed:', error);
            this.error = error;
          },
        });
    },
  },
};
</script>

<style lang="css">
.threads-current {
  margin-bottom: 0 !important;
}
</style>
