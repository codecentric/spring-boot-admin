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
  <sba-instance-section :error="error" :loading="!hasLoaded">
    <template #before>
      <sba-sticky-subnav>
        <div class="flex gap-2">
          <sba-input
            v-model="filter.uri"
            :placeholder="$t('term.filter')"
            class="flex-1"
            name="filter"
            type="search"
          >
            <template #prepend>
              <font-awesome-icon icon="filter" />
            </template>
            <template #append>
              <span class="button is-static">
                <span v-text="filteredTraces.length" />
                /
                <span v-text="traces.length" />
              </span>
            </template>
          </sba-input>

          <sba-input v-model="limit" :min="0" class="w-32" type="number">
            <template #prepend>
              {{ $t('instances.httptrace.limit') }}
            </template>
          </sba-input>

          <div class="grid grid-rows-2 grid-flow-col gap-x-2 text-sm">
            <sba-checkbox
              v-model="filter.showSuccess"
              :label="$t('instances.httptrace.filter.success')"
            />
            <sba-checkbox
              v-model="filter.showClientErrors"
              :label="$t('instances.httptrace.filter.client_errors')"
            />
            <sba-checkbox
              v-model="filter.showServerErrors"
              :label="$t('instances.httptrace.filter.server_errors')"
            />
            <sba-checkbox
              v-if="actuatorPath"
              v-model="filter.excludeActuator"
              :label="
                $t('instances.httptrace.filter.exclude_actuator', {
                  actuator: actuatorPath,
                })
              "
            />
          </div>
        </div>
      </sba-sticky-subnav>
    </template>

    <sba-panel>
      <sba-traces-chart
        :traces="filteredTraces"
        class="mb-6"
        @selected="updateSelection"
      />

      <sba-traces-list
        :new-traces-count="newTracesCount"
        :traces="listedTraces"
        @show-new-traces="showNewTraces"
      />
    </sba-panel>
  </sba-instance-section>
</template>

<script>
import { debounce, mapKeys } from 'lodash-es';
import moment from 'moment';
import { take } from 'rxjs/operators';

import SbaCheckbox from '@/components/sba-checkbox';

import subscribing from '@/mixins/subscribing';
import Instance from '@/services/instance';
import { concatMap, delay, retryWhen, timer } from '@/utils/rxjs';
import { VIEW_GROUP } from '@/views/ViewGroup';
import sbaTracesChart from '@/views/instances/httptrace/traces-chart';
import sbaTracesList from '@/views/instances/httptrace/traces-list';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

const addToFilter = (oldFilter, addedFilter) =>
  !oldFilter
    ? addedFilter
    : (val, key) => oldFilter(val, key) && addedFilter(val, key);

const normalize = (obj) => mapKeys(obj, (value, key) => key.toLowerCase());

class Trace {
  constructor({ timestamp, request, response, ...trace }) {
    Object.assign(this, trace);
    this.timestamp = moment(timestamp);
    this.request = { ...request, headers: normalize(request.headers) };
    this.response = response
      ? { ...response, headers: normalize(response.headers) }
      : null;
  }

  get key() {
    return `${this.timestamp.valueOf()}-${this.request.method}-${
      this.request.uri
    }`;
  }

  get contentLengthResponse() {
    return this.extractContentLength(this.response);
  }

  get contentLengthRequest() {
    return this.extractContentLength(this.request);
  }

  get contentTypeResponse() {
    return this.extractContentType(this.response);
  }

  get contentTypeRequest() {
    return this.extractContentType(this.request);
  }

  extractContentLength(origin) {
    const contentLength =
      origin &&
      origin.headers['content-length'] &&
      origin.headers['content-length'][0];
    if (contentLength && /^\d+$/.test(contentLength)) {
      return parseInt(contentLength);
    }
    return null;
  }

  extractContentType(origin) {
    const contentType =
      origin &&
      origin.headers['content-type'] &&
      origin.headers['content-type'][0];
    if (contentType) {
      const idx = contentType.indexOf(';');
      return idx >= 0 ? contentType.substring(0, idx) : contentType;
    }
    return null;
  }

  compareTo(other) {
    return this.timestamp - other.timestamp;
  }

  isPending() {
    return !this.response;
  }

  isSuccess() {
    return this.response && this.response.status <= 399;
  }

  isClientError() {
    return (
      this.response &&
      this.response.status >= 400 &&
      this.response.status <= 499
    );
  }

  isServerError() {
    return (
      this.response &&
      this.response.status >= 500 &&
      this.response.status <= 599
    );
  }
}

export default {
  components: {
    SbaCheckbox,
    SbaInstanceSection,
    sbaTracesList,
    sbaTracesChart,
  },
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
    traces: [],
    listOffset: 0,
    filter: {
      excludeActuator: true,
      showSuccess: true,
      showClientErrors: true,
      showServerErrors: true,
      uri: null,
    },
    limit: 1000,
    selection: null,
  }),
  computed: {
    actuatorPath() {
      if (
        this.instance.registration.managementUrl.includes(
          this.instance.registration.serviceUrl,
        )
      ) {
        const appendix = this.instance.registration.managementUrl.substring(
          this.instance.registration.serviceUrl.length,
        );
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
      return this.selection
        ? 0
        : this.filterTraces(this.traces.slice(0, this.listOffset)).length;
    },
    listedTraces() {
      const traces = this.filterTraces(this.traces.slice(this.listOffset));
      if (!this.selection) {
        return traces;
      }
      const [start, end] = this.selection;
      return traces.filter(
        (trace) =>
          !trace.timestamp.isBefore(start) && !trace.timestamp.isAfter(end),
      );
    },
    lastTimestamp() {
      return this.traces.length > 0 ? this.traces[0].timestamp : moment(0);
    },
  },
  watch: {
    limit: debounce(function (value) {
      if (this.traces.length > value) {
        this.traces = Object.freeze(this.traces.slice(0, value));
      }
    }, 250),
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
      const traces = response.data.traces
        .map((trace) => new Trace(trace))
        .filter((trace) => trace.timestamp.isAfter(this.lastTimestamp));
      traces.sort((a, b) => -1 * a.compareTo(b));
      return traces;
    },
    createSubscription() {
      return timer(0, 5000)
        .pipe(
          concatMap(this.fetchHttptrace),
          retryWhen((err) => {
            return err.pipe(delay(1000), take(2));
          }),
        )
        .subscribe({
          next: (traces) => {
            this.hasLoaded = true;
            if (this.traces.length > 0) {
              this.listOffset += traces.length;
            }
            this.traces = [...traces, ...this.traces].slice(0, this.limit);
          },
          error: (error) => {
            this.hasLoaded = true;
            console.warn('Fetching traces failed:', error);
            this.error = error;
          },
        });
    },
    filterTraces(traces) {
      let filterFn = null;
      if (this.actuatorPath !== null && this.filter.excludeActuator) {
        filterFn = addToFilter(
          filterFn,
          (trace) => !trace.request.uri.includes(this.actuatorPath),
        );
      }
      if (this.filter.uri) {
        const normalizedFilter = this.filter.uri.toLowerCase();
        filterFn = addToFilter(filterFn, (trace) =>
          trace.request.uri.toLowerCase().includes(normalizedFilter),
        );
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
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/httptrace',
      parent: 'instances',
      path: 'httptrace',
      component: this,
      label: 'instances.httptrace.label',
      group: VIEW_GROUP.WEB,
      order: 500,
      isEnabled: ({ instance }) => instance.hasEndpoint('httptrace'),
    });
  },
};
</script>
<style lang="css">
.httptraces__limit {
  width: 5em;
}
</style>
