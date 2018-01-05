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
    <section class="section" :class="{ 'is-loading' : !traces}">
        <div class="container" v-if="traces">
            <div class="content" v-if="traces.length > 0">
                <div class="field-body">
                    <div class="field has-addons">
                        <p class="control is-expanded">
                            <input class="input" type="text" placeholder="path" v-model="filter">
                        </p>
                        <p class="control">
                    <span class="button is-static">
                        <span v-text="filteredTraces.length"></span>
                        /
                        <span v-text="traces.length"></span>
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
                                exclude <span v-text="actuatorPath"></span>/**
                            </label>
                        </div>
                    </div>
                </div>
                <sba-traces-chart :traces="filteredTraces" @selected="(d) => selection = d"></sba-traces-chart>
                <sba-traces-list :traces="selectedTraces"></sba-traces-list>
            </div>
            <div class="content" v-else>
                <p class="is-muted">No traces recorded.</p>
            </div>
        </div>
    </section>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import moment from 'moment';
  import sbaTracesChart from './traces-chart';
  import sbaTracesList from './traces-list';

  const addToFilter = (oldFilter, addedFilter) =>
    !oldFilter
      ? addedFilter
      : (val, key) => oldFilter(val, key) && addedFilter(val, key);

  class Trace {
    constructor(timestamp, info) {
      this.info = info;
      this.timestamp = moment(timestamp);
    }

    get key() {
      return `${this.timestamp}-${this.method}-${this.path}`;
    }

    get method() {
      return this.info.method;
    }

    get path() {
      return this.info.path;
    }

    get status() {
      return this.info.headers.response.status;
    }

    get timeTaken() {
      const timeTaken = this.info.timeTaken;
      if (timeTaken && /^\d+$/.test(timeTaken)) {
        return parseInt(timeTaken);
      }
    }

    get contentLength() {
      const contentLength = this.info.headers.response['Content-Length'];
      if (contentLength && /^\d+$/.test(contentLength)) {
        return parseInt(contentLength);
      }
    }

    get contentType() {
      const contentType = this.info.headers.response['Content-Type'];
      if (contentType) {
        const idx = contentType.indexOf(';');
        return idx >= 0 ? contentType.substring(0, idx) : contentType;
      }
    }

    isSuccess() {
      return this.status <= 299
    }

    isClientError() {
      return this.status >= 400 && this.status <= 499
    }

    isServerError() {
      return this.status >= 500 && this.status <= 599
    }
  }

  export default {
    props: ['instance'],
    mixins: [subscribing],
    components: {
      sbaTracesList, sbaTracesChart
    },
    data: () => ({
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
        if (this.instance.registration.managementUrl.indexOf(this.instance.registration.serviceUrl) >= 0) {
          const appendix = this.instance.registration.managementUrl.substring(this.instance.registration.serviceUrl.length);
          if (appendix.length > 0) {
            return `/${appendix}`;
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
    watch: {
      instance() {
        this.subscribe();
      },
    },
    methods: {
      createSubscription() {
        const vm = this;
        if (this.instance) {
          return this.instance.streamTrace(5 * 1000)
            .subscribe({
              next: rawTraces => {
                const traces = rawTraces.map(trace => new Trace(trace.timestamp, trace.info));
                vm.traces = vm.traces ? traces.concat(vm.traces) : traces;
              },
              errors: err => {
                vm.unsubscribe();
              }
            });
        }
      },
      getFilterFn() {
        let filterFn = null;
        if (this.actuatorPath !== null && this.excludeActuator) {
          filterFn = addToFilter(filterFn, (trace) => trace.path.indexOf(this.actuatorPath) !== 0);
        }
        if (this.filter) {
          const normalizedFilter = this.filter.toLowerCase();
          filterFn = addToFilter(filterFn, (trace) => trace.path.toLowerCase().indexOf(normalizedFilter) >= 0);
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
    }
  }
</script>