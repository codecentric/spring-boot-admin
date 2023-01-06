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
    :title="$t('instances.details.memory.title') + `: ${name}`"
  >
    <div>
      <sba-alert v-if="error" :error="error" :title="$t('term.fetch_failed')" />

      <div v-if="current" class="flex w-full">
        <div v-if="current.metaspace" class="flex-1 text-center">
          <p
            class="font-bold"
            v-text="$t('instances.details.memory.metaspace')"
          />
          <p v-text="prettyBytes(current.metaspace)" />
        </div>
        <div class="flex-1 text-center">
          <p class="font-bold" v-text="$t('instances.details.memory.used')" />
          <p v-text="prettyBytes(current.used)" />
        </div>
        <div class="flex-1 text-center">
          <p class="font-bold" v-text="$t('instances.details.memory.size')" />
          <p v-text="prettyBytes(current.committed)" />
        </div>
        <div v-if="current.max >= 0" class="flex-1 text-center">
          <p class="font-bold" v-text="$t('instances.details.memory.max')" />
          <p v-text="prettyBytes(current.max)" />
        </div>
      </div>

      <mem-chart v-if="chartData.length > 0" :data="chartData" />
    </div>
  </sba-panel>
</template>

<script>
import moment from 'moment';
import prettyBytes from 'pretty-bytes';
import { concatMap, delay, retryWhen, timer } from 'rxjs';
import { take } from 'rxjs/operators';

import subscribing from '@/mixins/subscribing';
import Instance from '@/services/instance';
import memChart from '@/views/instances/details/mem-chart';

export default {
  name: 'DetailsMemory',
  components: { memChart },
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
    type: {
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
  computed: {
    name() {
      switch (this.type) {
        case 'heap':
          return 'Heap';
        case 'nonheap':
          return 'Non heap';
        default:
          return this.type;
      }
    },
  },
  methods: {
    prettyBytes,
    async fetchMetrics() {
      const responseMax = this.instance.fetchMetric('jvm.memory.max', {
        area: this.type,
      });
      const responseUsed = this.instance.fetchMetric('jvm.memory.used', {
        area: this.type,
      });
      const hasMetaspace = (await responseUsed).data.availableTags.some(
        (tag) => tag.tag === 'id' && tag.values.includes('Metaspace')
      );
      const responeMetaspace =
        this.type === 'nonheap' && hasMetaspace
          ? this.instance.fetchMetric('jvm.memory.used', {
              area: this.type,
              id: 'Metaspace',
            })
          : null;
      const responseCommitted = this.instance.fetchMetric(
        'jvm.memory.committed',
        { area: this.type }
      );
      return {
        max: (await responseMax).data.measurements[0].value,
        used: (await responseUsed).data.measurements[0].value,
        metaspace: responeMetaspace
          ? (await responeMetaspace).data.measurements[0].value
          : null,
        committed: (await responseCommitted).data.measurements[0].value,
      };
    },
    createSubscription() {
      const vm = this;
      return timer(0, 1000)
        .pipe(
          concatMap(this.fetchMetrics),
          retryWhen((err) => {
            return err.pipe(delay(1000), take(5));
          })
        )
        .subscribe({
          next: (data) => {
            vm.hasLoaded = true;
            vm.current = data;
            vm.chartData.push({ ...data, timestamp: moment().valueOf() });
          },
          error: (error) => {
            vm.hasLoaded = true;
            console.warn('Fetching memory metrics failed:', error);
            vm.error = error;
          },
        });
    },
  },
};
</script>

<style lang="css">
.memory-current {
  margin-bottom: 0 !important;
}
</style>
