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
  <section class="section">
    <div v-if="error" class="message is-danger">
      <div class="message-body">
        <strong>
          <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
          Fetching metrics failed.
        </strong>
        <p v-text="error.message" />
      </div>
    </div>
    <div v-if="isOldMetrics" class="message is-warning">
      <div class="message-body">
        Metrics are not supported for Spring Boot 1.x applications.
      </div>
    </div>
    <form @submit.prevent="handleSubmit" class="field" v-else-if="availableMetrics.length > 0">
      <div class="field">
        <div class="control">
          <div class="select">
            <select v-model="selectedMetric">
              <option v-for="metric in availableMetrics" v-text="metric" :key="metric" />
            </select>
          </div>
        </div>
      </div>
      <div>
        <p v-if="stateFetchingTags === 'executing'" class="is-loading">Fetching available tags</p>

        <div class="box" v-if="availableTags">
          <div class="field is-horizontal" v-for="tag in availableTags" :key="tag.tag">
            <div class="field-label">
              <label class="label" v-text="tag.tag" />
            </div>
            <div class="field-body">
              <div class="control">
                <div class="select">
                  <select v-model="selectedTags[tag.tag]">
                    <option :value="undefined">-</option>
                    <option v-for="value in tag.values" :key="value" :value="value" v-text="value" />
                  </select>
                </div>
              </div>
            </div>
          </div>
          <p v-if="availableTags && availableTags.length === 0">
            No tags available.
          </p>
          <div class="field is-grouped is-grouped-right">
            <div class="control">
              <button type="submit" class="button is-primary">Add Metric</button>
            </div>
          </div>
        </div>
      </div>
    </form>

    <metric v-for="metric in metrics"
            :key="metric.name"
            :metric-name="metric.name"
            :tag-selections="metric.tagSelections"
            :statistic-types="metric.types"
            :instance="instance"
            @remove="removeMetric"
            @type-select="handleTypeSelect"
    />
  </section>
</template>

<script>
  import Instance from '@/services/instance';
  import sortBy from 'lodash/sortBy';
  import Metric from './metric';

  export default {
    components: {Metric},
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    data: () => ({
      metrics: [],
      error: null,
      availableMetrics: [],
      selectedMetric: null,
      stateFetchingTags: null,
      availableTags: null,
      selectedTags: null,
      isOldMetrics: false
    }),
    created() {
      this.fetchMetricIndex();
      this.metrics = this.loadMetrics();
    },
    watch: {
      selectedMetric: 'fetchAvailableTags',
      metrics: {
        deep: true,
        handler(value) {
          this.persistMetrics(value);
        }
      }
    },
    methods: {
      handleSubmit() {
        this.addMetric(this.selectedMetric, this.selectedTags)
      },
      handleTypeSelect(metricName, statistic, type) {
        const metric = this.metrics.find(m => m.name === metricName);
        if (metric) {
          metric.types = {...metric.types, [statistic]: type}
        }
      },
      removeMetric(metricName, idxTagSelection) {
        const idxMetric = this.metrics.findIndex(m => m.name === metricName);
        if (idxMetric >= 0) {
          const metric = this.metrics[idxMetric];
          if (idxTagSelection < metric.tagSelections.length) {
            metric.tagSelections.splice(idxTagSelection, 1);
          }
          if (metric.tagSelections.length === 0) {
            this.metrics.splice(idxMetric, 1)
          }
        }
      },
      addMetric(metricName, tagSelection = {}) {
        if (metricName) {
          const metric = this.metrics.find(m => m.name === metricName);
          if (metric) {
            metric.tagSelections = [...metric.tagSelections, {...tagSelection}]
          } else {
            this.metrics = sortBy([...this.metrics, {
              name: metricName,
              tagSelections: [{...tagSelection}],
              types: {}
            }], [m => m.name]);
          }
        }
      },
      loadMetrics() {
        if (window.localStorage) {
          let persistedMetrics = localStorage.getItem(`applications/${this.instance.registration.name}/metrics`);
          if (persistedMetrics) {
            return JSON.parse(persistedMetrics);
          }
        }
        return [];
      },
      persistMetrics(value) {
        if (window.localStorage) {
          localStorage.setItem(`applications/${this.instance.registration.name}/metrics`, JSON.stringify(value));
        }
      },
      async fetchMetricIndex() {
        this.error = null;
        try {
          const res = await this.instance.fetchMetrics();
          if (res.headers['content-type'].includes('application/vnd.spring-boot.actuator.v2')) {
            this.availableMetrics = res.data.names;
            this.availableMetrics.sort();
            this.selectedMetric = this.availableMetrics[0];
          } else {
            this.isOldMetrics = true;
          }
        } catch (error) {
          console.warn('Fetching metric index failed:', error);
          this.hasLoaded = true;
          this.error = error;
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
          this.availableTags.forEach(t => this.selectedTags[t.tag] = undefined);
        } catch (error) {
          console.warn('Fetching metric tags failed:', error);
          this.stateFetchingTags = 'failed';
        }
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/metrics',
        parent: 'instances',
        path: 'metrics',
        component: this,
        label: 'Metrics',
        group: 'Insights',
        order: 50,
        isEnabled: ({instance}) => instance.hasEndpoint('metrics')
      });
    }
  }
</script>

