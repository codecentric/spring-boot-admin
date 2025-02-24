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
  <sba-instance-section :error="error" :loading="!hasLoaded">
    <sba-panel v-if="!isOldMetrics && availableMetrics.length > 0">
      <form class="grid grid-cols-6 gap-6">
        <div class="col-span-3">
          <div>
            <label
              class="block text-sm font-medium text-gray-700"
              for="metric"
              v-text="$t('instances.metrics.label')"
            />
            <div class="mt-1 relative rounded-md shadow-sm">
              <select
                id="metric"
                v-model="selectedMetric"
                class="focus:ring-indigo-500 focus:border-indigo-500 block w-full sm:text-sm border-gray-300 rounded-md"
              >
                <option
                  v-for="metric in availableMetrics"
                  :key="metric"
                  v-text="metric"
                />
              </select>
            </div>
          </div>
        </div>
        <div class="col-span-3 space-y-3">
          <template v-if="availableTags">
            <div v-for="tag in availableTags" :key="tag.tag">
              <label
                class="block text-sm font-medium text-gray-700"
                for="metric2"
                >{{ tag.tag }}</label
              >
              <div class="mt-1 relative rounded-md shadow-sm">
                <select
                  id="metric2"
                  v-model="selectedTags[tag.tag]"
                  class="focus:ring-indigo-500 focus:border-indigo-500 block w-full sm:text-sm border-gray-300 rounded-md"
                >
                  <option :value="undefined">-</option>
                  <option
                    v-for="value in tag.values"
                    :key="value"
                    :value="value"
                    v-text="value"
                  />
                </select>
              </div>
            </div>
          </template>
        </div>
      </form>

      <template #footer>
        <div class="text-right">
          <sba-button type="primary" @click="handleSubmit">
            {{ $t('instances.metrics.add_metric') }}
          </sba-button>
        </div>
      </template>
    </sba-panel>

    <p
      v-if="stateFetchingTags === 'executing'"
      class="is-loading"
      v-text="$t('instances.metrics.fetching_tags')"
    />

    <metric
      v-for="(metric, index) in metrics"
      :key="metric.name"
      :index="index"
      :instance="instance"
      :metric-name="metric.name"
      :statistic-types="metric.types"
      :tag-selections="metric.tagSelections"
      @remove="removeMetric"
      @type-select="handleTypeSelect"
    />
  </sba-instance-section>
</template>

<script>
import { sortBy } from 'lodash-es';

import SbaButton from '@/components/sba-button.vue';
import SbaPanel from '@/components/sba-panel.vue';

import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import Metric from '@/views/instances/metrics/metric';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

const ApiVersion = Object.freeze({
  V2: 'application/vnd.spring-boot.actuator.v2',
  V3: 'application/vnd.spring-boot.actuator.v3',
});

function isActuatorApiVersionSupported(headerContentType) {
  return (
    headerContentType.includes(ApiVersion.V2) ||
    headerContentType.includes(ApiVersion.V3)
  );
}

export default {
  components: { SbaButton, SbaPanel, SbaInstanceSection, Metric },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    metrics: [],
    error: null,
    availableMetrics: [],
    selectedMetric: null,
    stateFetchingTags: null,
    availableTags: null,
    selectedTags: null,
    isOldMetrics: false,
    hasLoaded: false,
  }),
  watch: {
    selectedMetric: 'fetchAvailableTags',
    metrics: {
      deep: true,
      handler(value) {
        this.persistMetrics(value);
      },
    },
  },
  created() {
    this.fetchMetricIndex();
    this.metrics = this.loadMetrics();
  },
  methods: {
    handleSubmit() {
      this.addMetric(this.selectedMetric, this.selectedTags);
    },
    handleTypeSelect(metricName, statistic, type) {
      const metric = this.metrics.find((m) => m.name === metricName);
      if (metric) {
        metric.types = { ...metric.types, [statistic]: type };
      }
    },
    removeMetric(metricName, idxTagSelection) {
      const idxMetric = this.metrics.findIndex((m) => m.name === metricName);
      if (idxMetric >= 0) {
        const metric = this.metrics[idxMetric];
        if (idxTagSelection < metric.tagSelections.length) {
          metric.tagSelections.splice(idxTagSelection, 1);
        }
        if (metric.tagSelections.length === 0) {
          this.metrics.splice(idxMetric, 1);
        }
      }
    },
    addMetric(metricName, tagSelection = {}) {
      if (metricName) {
        const metric = this.metrics.find((m) => m.name === metricName);
        if (metric) {
          metric.tagSelections = [...metric.tagSelections, { ...tagSelection }];
        } else {
          this.metrics = sortBy(
            [
              ...this.metrics,
              {
                name: metricName,
                tagSelections: [{ ...tagSelection }],
                types: {},
              },
            ],
            [(m) => m.name],
          );
        }
      }
    },
    loadMetrics() {
      if (window.localStorage) {
        let persistedMetrics = localStorage.getItem(
          `applications/${this.instance.registration.name}/metrics`,
        );
        if (persistedMetrics) {
          return JSON.parse(persistedMetrics);
        }
      }
      return [];
    },
    persistMetrics(value) {
      if (window.localStorage) {
        localStorage.setItem(
          `applications/${this.instance.registration.name}/metrics`,
          JSON.stringify(value),
        );
      }
    },
    async fetchMetricIndex() {
      this.error = null;
      try {
        const res = await this.instance.fetchMetrics();
        if (isActuatorApiVersionSupported(res.headers['content-type'])) {
          this.availableMetrics = res.data.names;
          this.availableMetrics.sort();
          this.selectedMetric = this.availableMetrics[0];
        } else {
          this.error = new Error(
            this.$t('instances.metrics.metrics_not_supported_spring_boot_1'),
          );
          this.isOldMetrics = true;
        }
      } catch (error) {
        console.warn('Fetching metric index failed:', error);
        this.error = error;
      } finally {
        this.hasLoaded = true;
      }
    },
    async fetchAvailableTags(metricName) {
      this.availableTags = null;
      this.stateFetchingTags = 'executing';
      try {
        const response = await this.instance.fetchMetric(metricName);
        this.availableTags = response.data.availableTags;
        this.stateFetchingTags = 'completed';
        this.selectedTags = {};
        if (this.availableTags) {
          this.availableTags.forEach(
            (t) => (this.selectedTags[t.tag] = undefined),
          );
        }
      } catch (error) {
        console.warn('Fetching metric tags failed:', error);
        this.stateFetchingTags = 'failed';
      }
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/metrics',
      parent: 'instances',
      path: 'metrics',
      component: this,
      label: 'instances.metrics.label',
      group: VIEW_GROUP.INSIGHTS,
      order: 50,
      isEnabled: ({ instance }) => instance.hasEndpoint('metrics'),
    });
  },
};
</script>
