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
  <table class="metrics table is-fullwidth">
    <thead>
      <tr>
        <th class="metrics__label" :title="description" v-text="metricName" />
        <th class="metrics__statistic-name"
            v-for="statistic in statistics"
            :key="`head-${statistic}`"
        >
          <span v-text="statistic" />
          <div class="select is-small is-pulled-right">
            <select :value="statisticTypes[statistic]"
                    @change="$emit('type-select', metricName, statistic, $event.target.value)"
            >
              <option :value="undefined">
                -
              </option>
              <option value="integer" v-text="$t('term.integer')" />
              <option value="float" v-text="$t('term.float')" />
              <option value="duration" v-text="$t('term.duration')" />
              <option value="millis" v-text="$t('term.milliseconds')" />
              <option value="bytes" v-text="$t('term.bytes')" />
            </select>
          </div>
        </th>
        <td />
      </tr>
    </thead>
    <tbody>
      <tr v-for="(tags, idx) in tagSelections" :key="idx">
        <td class="metrics__label">
          <span v-text="getLabel(tags)" />
          <span class="has-text-warning" v-if="errors[idx]" :title="errors[idx]">
            <font-awesome-icon icon="exclamation-triangle" />
          </span>
        </td>
        <td class="metrics__statistic-value"
            v-for="statistic in statistics"
            :key="`value-${idx}-${statistic}`"
            v-text="getValue(measurements[idx], statistic)"
        />
        <td class="metrics__actions">
          <sba-icon-button :icon="'trash'" @click.stop="handleRemove(idx)" />
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import Instance from '@/services/instance';
  import {concatMap, from, timer} from '@/utils/rxjs';
  import moment from 'moment';
  import prettyBytes from 'pretty-bytes';
  import i18n from '@/i18n';

  const formatDuration = (value, baseUnit) => {
    const duration = moment.duration(toMillis(value, baseUnit));
    return `${Math.floor(duration.asDays())}d ${duration.hours()}h ${duration.minutes()}m ${duration.seconds()}s`;
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
    mixins: [subscribing],
    props: {
      metricName: {
        type: String,
        required: true
      },
      instance: {
        type: Instance,
        required: true
      },
      tagSelections: {
        type: Array,
        default: () => [{}]
      },
      statisticTypes: {
        type: Object,
        default: () => ({})
      }
    },
    data: () => ({
      description: '',
      baseUnit: undefined,
      measurements: [],
      statistics: [],
      errors: [],
    }),
    methods: {
      handleRemove(idx) {
        this.$emit('remove', this.metricName, idx);
      },
      getValue(measurements, statistic) {
        const measurement = measurements && measurements.find(m => m.statistic === statistic);
        if (!measurement) {
          return undefined;
        }
        const type = this.statisticTypes[statistic];
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
        return Object.entries(tags).filter(([, value]) => typeof value !== 'undefined')
          .map(pair => pair.join(':'))
          .join('\n') || i18n.t('instances.metrics.no_tags');
      },
      async fetchMetric(tags, idx) {
        try {
          const response = await this.instance.fetchMetric(this.metricName, tags);
          this.$set(this.errors, idx, null);
          this.$set(this.measurements, idx, response.data.measurements);
          if (idx === 0) {
            this.description = response.data.description;
            this.baseUnit = response.data.baseUnit;
            this.statistics = response.data.measurements.map(m => m.statistic);
          }
        } catch (error) {
          console.warn(`Fetching metric ${this.metricName} failed:`, error);
          this.$set(this.errors, idx, error);
        }
      },
      fetchAllTags() {
        return from(this.tagSelections).pipe(concatMap(this.fetchMetric));
      },
      createSubscription() {
        const vm = this;
        return timer(0, 2500)
          .pipe(concatMap(vm.fetchAllTags))
          .subscribe({
            next: () => {
            }
          });
      }
    },
    watch: {
      tagSelections(newVal, oldVal) {
        newVal.map((v, i) => [v, i])
          .filter(([v]) => !oldVal.includes(v))
          .forEach(([v, i]) => this.fetchMetric(v, i));
      }
    }
  }
</script>
<style lang="scss">
  table .metrics {
    &__label {
      width: 300px;
      white-space: pre-wrap;
    }
    &__actions {
      width: 1px;
      vertical-align: middle;
    }
    &__statistic-name * {
      vertical-align: middle;
    }

    &__statistic-value {
      text-align: right;
      vertical-align: middle;
    }
  }
</style>
