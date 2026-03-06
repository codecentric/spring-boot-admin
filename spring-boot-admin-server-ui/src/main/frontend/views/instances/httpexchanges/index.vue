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
                <span v-text="filteredExchanges.length" />
                /
                <span v-text="exchanges.length" />
              </span>
            </template>
          </sba-input>

          <sba-input
            v-model="limit"
            name="limit"
            :min="0"
            class="w-32"
            type="number"
          >
            <template #prepend>
              {{ $t('instances.httpexchanges.limit') }}
            </template>
          </sba-input>

          <div class="grid grid-rows-2 grid-flow-col gap-x-2 text-sm">
            <sba-checkbox
              v-model="filter.showSuccess"
              :label="$t('instances.httpexchanges.filter.success')"
            />
            <sba-checkbox
              v-model="filter.showClientErrors"
              :label="$t('instances.httpexchanges.filter.client_errors')"
            />
            <sba-checkbox
              v-model="filter.showServerErrors"
              :label="$t('instances.httpexchanges.filter.server_errors')"
            />
            <sba-checkbox
              v-model="filter.excludeActuator"
              :label="
                $t('instances.httpexchanges.filter.exclude_actuator', {
                  actuator: '/actuator',
                })
              "
            />
          </div>
          <div>
            <sba-button
              :title="$t('instances.httpexchanges.auto_follow')"
              :primary="autoFollow"
              @click="autoFollow = !autoFollow"
            >
              <font-awesome-icon :icon="faArrowsDownToLine" />
              <span
                class="sr-only"
                v-text="$t('instances.httpexchanges.auto_follow')"
              />
            </sba-button>
          </div>
        </div>
      </sba-sticky-subnav>
    </template>

    <sba-panel>
      <sba-exchanges-chart :exchanges="listedExchanges" class="mb-6" />
    </sba-panel>

    <sba-panel seamless>
      <sba-exchanges-list :exchanges="listedExchanges" />
    </sba-panel>
  </sba-instance-section>
</template>

<script>
import { faArrowsDownToLine } from '@fortawesome/free-solid-svg-icons';
import { debounce } from 'lodash-es';
import moment from 'moment';
import { take } from 'rxjs/operators';

import SbaCheckbox from '@/components/sba-checkbox';

import subscribing from '@/mixins/subscribing';
import Instance from '@/services/instance';
import { concatMap, delay, retryWhen, timer } from '@/utils/rxjs';
import { VIEW_GROUP } from '@/views/ViewGroup';
import { Exchange } from '@/views/instances/httpexchanges/Exchange';
import SbaExchangesChart from '@/views/instances/httpexchanges/exchanges-chart';
import SbaExchangesList from '@/views/instances/httpexchanges/exchanges-list';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

const addToFilter = (oldFilter, addedFilter) =>
  !oldFilter
    ? addedFilter
    : (val, key) => oldFilter(val, key) && addedFilter(val, key);

export default {
  components: {
    SbaCheckbox,
    SbaInstanceSection,
    SbaExchangesList,
    SbaExchangesChart,
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
    exchanges: [],
    listOffset: 0,
    faArrowsDownToLine,
    filter: {
      excludeActuator: true,
      showSuccess: true,
      showClientErrors: true,
      showServerErrors: true,
      uri: null,
    },
    limit: 1000,
    selection: null,
    autoFollow: true,
  }),
  computed: {
    filteredExchanges() {
      return this.filterExchanges(this.exchanges);
    },
    listedExchanges() {
      const exchanges = this.filterExchanges(
        this.exchanges.slice(this.listOffset),
      );
      if (!this.selection) {
        return exchanges;
      }
      const [start, end] = this.selection;
      return exchanges.filter(
        (exchange) =>
          !exchange.timestamp.isBefore(start) &&
          !exchange.timestamp.isAfter(end),
      );
    },
    lastTimestamp() {
      return this.exchanges.length > 0
        ? this.exchanges[0].timestamp
        : moment(0);
    },
  },
  watch: {
    limit: debounce(function (value) {
      if (this.exchanges.length > value) {
        this.exchanges = Object.freeze(this.exchanges.slice(0, value));
      }
    }, 250),
    autoFollow: function (value) {
      if (value) {
        this.showNewExchanges();
      }
    },
  },
  methods: {
    showNewExchanges() {
      this.listOffset = 0;
    },
    async fetchHttpExchanges() {
      const response = await this.instance.fetchHttpExchanges();
      const exchanges = response.data.exchanges
        .map((exchange) => new Exchange(exchange))
        .filter((exchange) => exchange.timestamp.isAfter(this.lastTimestamp));
      exchanges.sort((a, b) => -1 * a.compareTo(b));
      return exchanges;
    },
    createSubscription() {
      return timer(0, 5000)
        .pipe(
          concatMap(this.fetchHttpExchanges),
          retryWhen((err) => {
            return err.pipe(delay(1000), take(2));
          }),
        )
        .subscribe({
          next: (exchanges) => {
            this.hasLoaded = true;
            if (this.exchanges.length > 0) {
              this.listOffset += exchanges.length;
            }
            this.exchanges = [...exchanges, ...this.exchanges].slice(
              0,
              this.limit ?? 1000,
            );
            if (this.autoFollow && exchanges.length > 0) {
              this.showNewExchanges();
            }
          },
          error: (error) => {
            this.hasLoaded = true;
            console.warn('Fetching exchanges failed:', error);
            this.error = error;
          },
        });
    },
    filterExchanges(exchanges) {
      let filterFn = null;
      if (this.filter.excludeActuator) {
        filterFn = addToFilter(filterFn, (exchange) => {
          try {
            const uri = exchange.request.uri;
            const pathname = new URL(uri).pathname;
            const regex = /(^|\/instances\/[^\/]+)\/actuator(\/|$|\?)/;

            return !regex.test(pathname);
          } catch {
            return true;
          }
        });
      }
      if (this.filter.uri) {
        const normalizedFilter = this.filter.uri.toLowerCase();
        filterFn = addToFilter(filterFn, (exchange) =>
          exchange.request.uri.toLowerCase().includes(normalizedFilter),
        );
      }
      if (!this.filter.showSuccess) {
        filterFn = addToFilter(filterFn, (exchange) => !exchange.isSuccess());
      }
      if (!this.filter.showClientErrors) {
        filterFn = addToFilter(
          filterFn,
          (exchange) => !exchange.isClientError(),
        );
      }
      if (!this.filter.showServerErrors) {
        filterFn = addToFilter(
          filterFn,
          (exchange) => !exchange.isServerError(),
        );
      }
      return filterFn ? exchanges.filter(filterFn) : exchanges;
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/httpexchanges',
      parent: 'instances',
      path: 'httpexchanges',
      component: this,
      label: 'instances.httpexchanges.label',
      group: VIEW_GROUP.WEB,
      order: 500,
      isEnabled: ({ instance }) => instance.hasEndpoint('httpexchanges'),
    });
  },
};
</script>
