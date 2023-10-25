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
  <div>
    <sba-panel :header-sticks-below="'#navigation'" :title="metricName">
      <template #actions>
        <div
          v-for="statistic in statistics"
          :key="`head-${statistic}`"
          class="inline-flex items-center"
        >
          <span
            class="block font-medium text-gray-700 px-3"
            v-text="statistic"
          />

          <div class="relative rounded-md shadow-sm">
            <select
              :value="statisticTypes[statistic]"
              class="focus:ring-indigo-500 focus:border-indigo-500 block w-full text-sm border-gray-300 rounded-md"
              @change="
                $emit('type-select', metricName, statistic, $event.target.value)
              "
            >
              <option :value="undefined">-</option>
              <option value="integer" v-text="$t('term.integer')" />
              <option value="float" v-text="$t('term.float')" />
              <option value="duration" v-text="$t('term.duration')" />
              <option value="millis" v-text="$t('term.milliseconds')" />
              <option value="bytes" v-text="$t('term.bytes')" />
            </select>
          </div>
        </div>
      </template>

      <div class="-mx-4 -my-3">
        <div
          v-for="(tags, idx) in tagSelections"
          :key="idx"
          :class="{ 'bg-gray-50': idx % 2 !== 0 }"
          class="bg-white px-4 py-3 grid grid-cols-3 gap-4"
        >
          <div
            class="text-sm flex items-center font-medium text-gray-500 col-span-2"
          >
            <span
              :title="getLabel(tags)"
              class="whitespace-pre"
              v-text="getLabel(tags)"
            />
            <span
              v-if="errors[idx]"
              :title="errors[idx]"
              class="text-yellow-300 pl-1"
            >
              <font-awesome-icon icon="exclamation-triangle" />
            </span>
          </div>

          <div class="mt-1 text-sm text-gray-900">
            <div class="flex items-center justify-end">
              <span
                v-for="statistic in statistics"
                :key="`value-${idx}-${statistic}`"
                class="flex-1"
                v-text="getValue(measurements[idx], statistic)"
              />
              <sba-icon-button
                :icon="'trash'"
                class="self-end"
                @click.stop="handleRemove(idx)"
              />
            </div>
          </div>
        </div>
      </div>
    </sba-panel>
  </div>
</template>

<script lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import moment from 'moment';
import prettyBytes from 'pretty-bytes';
import { take } from 'rxjs/operators';
import { useI18n } from 'vue-i18n';

import SbaIconButton from '@/components/sba-icon-button.vue';
import SbaPanel from '@/components/sba-panel.vue';

import subscribing from '@/mixins/subscribing';
import Instance from '@/services/instance';
import { concatMap, delay, from, retryWhen, timer } from '@/utils/rxjs';

const formatDuration = (value, baseUnit) => {
  const duration = moment.duration(toMillis(value, baseUnit));
  return `${Math.floor(
    duration.asDays(),
  )}d ${duration.hours()}h ${duration.minutes()}m ${duration.seconds()}s`;
};

const formatMillis = (value, baseUnit) => {
  const duration = moment.duration(toMillis(value, baseUnit));
  return `${moment.duration(duration).asMilliseconds().toFixed(0)} ms`;
};

export const toMillis = (value, baseUnit) => {
  switch (baseUnit) {
    case 'nanoseconds':
      return value / 1000000;
    case 'microseconds':
      return value / 1000;
    case 'milliseconds':
      return value;
    case 'seconds':
    default:
      return value * 1000;
  }
};

export default {
  name: 'Metric',
  components: { SbaIconButton, FontAwesomeIcon, SbaPanel },
  mixins: [subscribing],
  props: {
    metricName: {
      type: String,
      required: true,
    },
    instance: {
      type: Instance,
      required: true,
    },
    tagSelections: {
      type: Array,
      default: () => [{}],
    },
    statisticTypes: {
      type: Object,
      default: () => ({}),
    },
    index: {
      type: Number,
      default: 0,
    },
  },
  emits: ['type-select', 'remove'],
  setup() {
    const i18n = useI18n();
    return {
      i18n,
    };
  },
  data: () => ({
    description: '',
    baseUnit: undefined,
    measurements: [],
    statistics: [],
    errors: [],
  }),
  watch: {
    tagSelections(newVal, oldVal) {
      newVal
        .map((v, i) => [v, i])
        .filter(([v]) => !oldVal.includes(v))
        .forEach(([v, i]) => this.fetchMetric(v, i));
    },
  },
  methods: {
    handleRemove(idx) {
      this.$emit('remove', this.metricName, idx);
    },
    getValue(measurements, statistic) {
      const measurement =
        measurements && measurements.find((m) => m.statistic === statistic);
      if (!measurement) {
        return undefined;
      }
      const type = this.statisticTypes?.[statistic];
      switch (type) {
        case 'integer':
          return measurement.value.toFixed(0);
        case 'float':
          return measurement.value.toFixed(4);
        case 'duration':
          return formatDuration(measurement.value, this.baseUnit);
        case 'millis':
          return formatMillis(measurement.value, this.baseUnit);
        case 'bytes':
          return prettyBytes(measurement.value);
        default:
          return measurement.value;
      }
    },
    getLabel(tags) {
      return (
        Object.entries(tags)
          .filter(([, value]) => typeof value !== 'undefined')
          .map((pair) => pair.join(':'))
          .join('\n') || this.i18n.t('instances.metrics.no_tags')
      );
    },
    async fetchMetric(tags, idx) {
      try {
        const response = await this.instance.fetchMetric(this.metricName, tags);
        this.errors[idx] = null;
        this.measurements[idx] = response.data.measurements;
        if (idx === 0) {
          this.description = response.data.description;
          this.baseUnit = response.data.baseUnit;
          this.statistics = response.data.measurements.map((m) => m.statistic);
        }
      } catch (error) {
        console.warn(`Fetching metric ${this.metricName} failed:`, error);
        this.errors[idx] = error;
      }
    },
    fetchAllTags() {
      return from(this.tagSelections || []).pipe(concatMap(this.fetchMetric));
    },
    createSubscription() {
      return timer(0, 2500)
        .pipe(
          concatMap(this.fetchAllTags),
          retryWhen((err) => err.pipe(delay(1000), take(2))),
        )
        .subscribe();
    },
  },
};
</script>
