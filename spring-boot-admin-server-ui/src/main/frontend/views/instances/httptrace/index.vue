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
  <section class="section" :class="{ 'is-loading' : !hasLoaded}">
    <template v-if="hasLoaded">
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
            Fetching traces failed.
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <template v-if="traces">
        <div class="field-body">
          <div class="field has-addons">
            <p class="control is-expanded">
              <input class="input" type="search" placeholder="path filter" v-model="filter">
            </p>
            <p class="control">
              <span class="button is-static">
                <span v-text="filteredTraces.length" />
                /
                <span v-text="traces.length" />
              </span>
            </p>
          </div>
        </div>
        <div class="field-body">
          <div class="field is-narrow">
            <div class="control">
              <label class="checkbox">
                <input type="checkbox" v-model="showSuccess">
                success
              </label>
            </div>
          </div>
          <div class="field is-narrow">
            <div class="control">
              <label class="checkbox">
                <input type="checkbox" v-model="showClientErrors">
                client errors
              </label>
            </div>
          </div>
          <div class="field is-narrow">
            <div class="control">
              <label class="checkbox">
                <input type="checkbox" v-model="showServerErrors">
                server errors
              </label>
            </div>
          </div>
          <div class="field is-narrow" v-if="actuatorPath">
            <div class="control">
              <label class="checkbox">
                <input type="checkbox" v-model="excludeActuator">
                exclude <span v-text="actuatorPath" />/**
              </label>
            </div>
          </div>
        </div>
        <sba-traces-chart :traces="filteredTraces" @selected="(d) => selection = d" />
        <sba-traces-list :traces="selectedTraces" />
      </template>
    </template>
  </section>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import Instance from '@/services/instance';
  import {concatMap, timer} from '@/utils/rxjs';
  import moment from 'moment';
  import sbaTracesChart from './traces-chart';
  import sbaTracesList from './traces-list';

  const addToFilter = (oldFilter, addedFilter) =>
    !oldFilter
      ? addedFilter
      : (val, key) => oldFilter(val, key) && addedFilter(val, key);

  class Trace {
    constructor({timestamp, ...trace}) {
      Object.assign(this, trace);
      this.timestamp = moment(timestamp);
    }

    get key() {
      return `${this.timestamp}-${this.request.method}-${this.request.uri}`;
    }

    get contentLength() {
      const contentLength = this.response.headers['Content-Length'] && this.response.headers['Content-Length'][0];
      if (contentLength && /^\d+$/.test(contentLength)) {
        return parseInt(contentLength);
      }
      return null;
    }

    get contentType() {
      const contentType = this.response.headers['Content-Type'] && this.response.headers['Content-Type'][0];
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
      return this.response.status <= 399
    }

    isClientError() {
      return this.response.status >= 400 && this.response.status <= 499
    }

    isServerError() {
      return this.response.status >= 500 && this.response.status <= 599
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
    // eslint-disable-next-line vue/no-unused-components
    components: {sbaTracesList, sbaTracesChart},
    data: () => ({
      hasLoaded: false,
      error: null,
      traces: null,
      filter: null,
      excludeActuator: true,
      showSuccess: true,
      showClientErrors: true,
      showServerErrors: true,
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
        const filterFn = this.getFilterFn();
        return filterFn ? this.traces.filter(filterFn) : this.traces;
      },
      selectedTraces() {
        if (this.selection === null) {
          return this.filteredTraces;
        }
        const [start, end] = this.selection;
        return this.filteredTraces.filter(trace => !trace.timestamp.isBefore(start) && !trace.timestamp.isAfter(end));
      }
    },
    methods: {
      async fetchHttptrace() {
        const response = await this.instance.fetchHttptrace();
        const traces = response.data.traces.map(trace => new Trace(trace)).filter(
          trace => trace.timestamp.isAfter(this.lastTimestamp)
        );
        traces.sort((a, b) => -1 * a.compareTo(b));
        if (traces.length > 0) {
          this.lastTimestamp = traces[0].timestamp;
        }
        return traces;
      },
      createSubscription() {
        const vm = this;
        vm.lastTimestamp = moment(0);
        vm.error = null;
        return timer(0, 5000)
          .pipe(concatMap(vm.fetchHttptrace))
          .subscribe({
            next: traces => {
              vm.hasLoaded = true;
              vm.traces = vm.traces ? traces.concat(vm.traces) : traces;
            },
            error: error => {
              vm.hasLoaded = true;
              console.warn('Fetching traces failed:', error);
              vm.error = error;
            }
          });
      },
      getFilterFn() {
        let filterFn = null;
        if (this.actuatorPath !== null && this.excludeActuator) {
          filterFn = addToFilter(filterFn, (trace) => !trace.request.uri.includes(this.actuatorPath));
        }
        if (this.filter) {
          const normalizedFilter = this.filter.toLowerCase();
          filterFn = addToFilter(filterFn, (trace) => trace.request.uri.toLowerCase().includes(normalizedFilter));
        }
        if (!this.showSuccess) {
          filterFn = addToFilter(filterFn, (trace) => !trace.isSuccess());
        }
        if (!this.showClientErrors) {
          filterFn = addToFilter(filterFn, (trace) => !trace.isClientError());
        }
        if (!this.showServerErrors) {
          filterFn = addToFilter(filterFn, (trace) => !trace.isServerError());
        }
        return filterFn;
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/httptrace',
        parent: 'instances',
        path: 'httptrace',
        component: this,
        label: 'Http Traces',
        group: 'Web',
        order: 500,
        isEnabled: ({instance}) => instance.hasEndpoint('httptrace')
      });
    }
  }
</script>
