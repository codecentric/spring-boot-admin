<!--
  - Copyright 2014-2020 the original author or authors.
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
  <section class="section" :class="{ 'is-loading' : !hasLoaded}">
    <template v-if="hasLoaded">
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
            <span v-text="$t('instances.httptrace.fetching_failed')" />
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <template v-if="hasLoaded">
        <div class="field is-horizontal">
          <div class="field-body">
            <div class="field has-addons">
              <p class="control is-expanded has-icons-left">
                <input
                  class="input"
                  type="search"
                  v-model="filter.uri"
                  :placeholder="$t('instances.httptrace.uri')"
                >
                <span class="icon is-small is-left">
                  <font-awesome-icon icon="filter" />
                </span>
              </p>
              <p class="control">
                <span class="button is-static">
                  <span v-text="filteredTraces.length" />
                  /
                  <span v-text="traces.length" />
                </span>
              </p>
            </div>
            <div class="field is-narrow has-addons">
              <p class="control">
                <span class="button is-static" v-text="$t('instances.httptrace.limit')" />
              </p>
              <p class="control">
                <input class="input httptraces__limit" min="0" type="number" placeholder="trace limit" v-model="limit">
              </p>
            </div>
          </div>
        </div>
        <div class="field-body">
          <div class="field is-narrow">
            <div class="control">
              <label class="checkbox">
                <input type="checkbox" v-model="filter.showSuccess">
                <span v-text="$t('instances.httptrace.filter.success')" />
              </label>
            </div>
          </div>
          <div class="field is-narrow">
            <div class="control">
              <label class="checkbox">
                <input type="checkbox" v-model="filter.showClientErrors">
                <span v-text="$t('instances.httptrace.filter.client_errors')" />
              </label>
            </div>
          </div>
          <div class="field is-narrow">
            <div class="control">
              <label class="checkbox">
                <input type="checkbox" v-model="filter.showServerErrors">
                <span v-text="$t('instances.httptrace.filter.server_errors')" />
              </label>
            </div>
          </div>
          <div class="field is-narrow" v-if="actuatorPath">
            <div class="control">
              <label class="checkbox">
                <input type="checkbox" v-model="filter.excludeActuator">
                <span v-text="$t('instances.httptrace.filter.exclude_actuator', {actuator: actuatorPath})" />
              </label>
            </div>
          </div>
        </div>
        <sba-traces-chart :traces="filteredTraces" @selected="updateSelection" />
        <sba-traces-list
          :new-traces-count="newTracesCount"
          :traces="listedTraces"
          @show-new-traces="showNewTraces"
        />
      </template>
    </template>
  </section>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import Instance from '@/services/instance';
  import {concatMap, timer} from '@/utils/rxjs';
  import debounce from 'lodash/debounce';
  import mapKeys from 'lodash/mapKeys';
  import moment from 'moment';
  import {VIEW_GROUP} from '../../index';
  import sbaTracesChart from './traces-chart';
  import sbaTracesList from './traces-list';

  const addToFilter = (oldFilter, addedFilter) =>
    !oldFilter
      ? addedFilter
      : (val, key) => oldFilter(val, key) && addedFilter(val, key);

  const normalize = (obj) => mapKeys(obj, (value, key) => key.toLowerCase());

  class Trace {
    constructor({timestamp, request, response, ...trace}) {
      Object.assign(this, trace);
      this.timestamp = moment(timestamp);
      this.request = {...request, headers: normalize(request.headers)};
      this.response = response ? {...response, headers: normalize(response.headers)} : null;
    }

    get key() {
      return `${this.timestamp.valueOf()}-${this.request.method}-${this.request.uri}`;
    }

    get contentLength() {
      const contentLength = this.response && this.response.headers['content-length'] && this.response.headers['content-length'][0];
      if (contentLength && /^\d+$/.test(contentLength)) {
        return parseInt(contentLength);
      }
      return null;
    }

    get contentType() {
      const contentType = this.response &&  this.response.headers['content-type'] && this.response.headers['content-type'][0];
      if (contentType) {
        const idx = contentType.indexOf(';');
        return idx >= 0 ? contentType.substring(0, idx) : contentType;
      }
      return null;
    }

    compareTo(other) {
      return this.timestamp - other.timestamp;
    }

    isSuccess() {
      return this.response && this.response.status <= 399
    }

    isClientError() {
      return this.response && this.response.status >= 400 && this.response.status <= 499
    }

    isServerError() {
      return this.response && this.response.status >= 500 && this.response.status <= 599
    }
  }

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    mixins: [subscribing],
    components: {sbaTracesList, sbaTracesChart},
    data: () => ({
      hasLoaded: false,
      error: null,
      traces: [],
      listOffset: 0,
      filter: {
        excludeActuator: true,
        showSuccess: true,
        showClientErrors: true,
        showServerErrors: true,
        uri: null
      },
      limit: 1000,
      selection: null
    }),
    computed: {
      actuatorPath() {
        if (this.instance.registration.managementUrl.includes(this.instance.registration.serviceUrl)) {
          const appendix = this.instance.registration.managementUrl.substring(this.instance.registration.serviceUrl.length);
          if (appendix.length > 0) {
            return appendix.startsWith('/') ? appendix : `/${appendix}`;
          }
        }
        return null;
      },
      filteredTraces() {
        return this.filterTraces(this.traces);
      },
      newTracesCount() {
        return this.selection ? 0 : this.filterTraces(this.traces.slice(0, this.listOffset)).length;
      },
      listedTraces() {
        const traces = this.filterTraces(this.traces.slice(this.listOffset));
        if (!this.selection) {
          return traces;
        }
        const [start, end] = this.selection;
        return traces.filter(trace => !trace.timestamp.isBefore(start) && !trace.timestamp.isAfter(end));
      },
      lastTimestamp() {
        return this.traces.length > 0 ? this.traces[0].timestamp : moment(0);
      }
    },
    watch: {
      limit: debounce(function (value) {
        if (this.traces.length > value) {
          this.traces = Object.freeze(this.traces.slice(0, value));
        }
      }, 250)
    },
    methods: {
      updateSelection(selection) {
        this.selection = selection;
        this.showNewTraces();
      },
      showNewTraces() {
        this.listOffset = 0;
      },
      async fetchHttptrace() {
        const response = await this.instance.fetchHttptrace();
        const traces = response.data.traces.map(trace => new Trace(trace))
          .filter(trace => trace.timestamp.isAfter(this.lastTimestamp));
        traces.sort((a, b) => -1 * a.compareTo(b));
        return traces;
      },
      createSubscription() {
        const vm = this;
        return timer(0, 5000)
          .pipe(concatMap(vm.fetchHttptrace))
          .subscribe({
            next: traces => {
              vm.hasLoaded = true;
              if (vm.traces.length > 0) {
                vm.listOffset += traces.length;
              }
              vm.traces = [...traces, ...vm.traces].slice(0, vm.limit);
            },
            error: error => {
              vm.hasLoaded = true;
              console.warn('Fetching traces failed:', error);
              vm.error = error;
            }
          });
      },
      filterTraces(traces) {
        let filterFn = null;
        if (this.actuatorPath !== null && this.filter.excludeActuator) {
          filterFn = addToFilter(filterFn, (trace) => !trace.request.uri.includes(this.actuatorPath));
        }
        if (this.filter.uri) {
          const normalizedFilter = this.filter.uri.toLowerCase();
          filterFn = addToFilter(filterFn, (trace) => trace.request.uri.toLowerCase().includes(normalizedFilter));
        }
        if (!this.filter.showSuccess) {
          filterFn = addToFilter(filterFn, (trace) => !trace.isSuccess());
        }
        if (!this.filter.showClientErrors) {
          filterFn = addToFilter(filterFn, (trace) => !trace.isClientError());
        }
        if (!this.filter.showServerErrors) {
          filterFn = addToFilter(filterFn, (trace) => !trace.isServerError());
        }
        return filterFn ? traces.filter(filterFn) : traces;
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/httptrace',
        parent: 'instances',
        path: 'httptrace',
        component: this,
        label: 'instances.httptrace.label',
        group: VIEW_GROUP.WEB,
        order: 500,
        isEnabled: ({instance}) => instance.hasEndpoint('httptrace')
      });
    }
  }
</script>
<style lang="scss">
  @import "~@/assets/css/utilities";

  .httptraces__limit {
    width: 5em;
  }
</style>
