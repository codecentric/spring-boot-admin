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
  <sba-instance-section :error="error" :loading="isLoading">
    <template #before>
      <sba-sticky-subnav>
        <div class="flex gap-2">
          <sba-input
            v-model.trim="filter.principal"
            name="filter_principal"
            type="search"
            :placeholder="$t('instances.auditevents.principal')"
          />
          <sba-input
            v-model="filter.type"
            name="filter_type"
            :list="[
              'AUTHENTICATION_FAILURE',
              'AUTHENTICATION_SUCCESS',
              'AUTHENTICATION_SWITCH',
              'AUTHORIZATION_FAILURE',
            ]"
            type="search"
            :placeholder="$t('instances.auditevents.type')"
          />

          <sba-input
            name="filter_datetime"
            type="datetime-local"
            placeholder="Date"
            :value="formatDate(filter.after)"
            @input="filter.after = parseDate($event.target.value)"
          />
        </div>
      </sba-sticky-subnav>
    </template>

    <sba-panel :seamless="true">
      <auditevents-list
        :instance="instance"
        :events="events"
        :is-loading="isLoading"
      />
    </sba-panel>
  </sba-instance-section>
</template>

<script>
import { uniqBy } from 'lodash-es';
import moment from 'moment';
import { Subject, concatMap, debounceTime, mergeWith, tap, timer } from 'rxjs';

import SbaInput from '@/components/sba-input';
import SbaPanel from '@/components/sba-panel';
import SbaStickySubnav from '@/components/sba-sticky-subnav';

import subscribing from '@/mixins/subscribing';
import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import AuditeventsList from '@/views/instances/auditevents/auditevents-list';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

class Auditevent {
  constructor({ timestamp, ...event }) {
    Object.assign(this, event);
    this.zonedTimestamp = timestamp;
    this.timestamp = moment(timestamp);
  }

  get key() {
    return `${this.zonedTimestamp}-${this.type}-${this.principal}`;
  }

  get remoteAddress() {
    return (
      (this.data && this.data.details && this.data.details.remoteAddress) ||
      null
    );
  }

  get sessionId() {
    return (
      (this.data && this.data.details && this.data.details.sessionId) || null
    );
  }

  isSuccess() {
    return this.type.toLowerCase().includes('success');
  }

  isFailure() {
    return this.type.toLowerCase().includes('failure');
  }
}

export default {
  components: {
    SbaInput,
    SbaInstanceSection,
    SbaStickySubnav,
    SbaPanel,
    AuditeventsList,
  },
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    isLoading: false,
    error: null,
    events: [],
    filter: {
      after: moment().startOf('day'),
      type: null,
      principal: null,
    },
  }),
  watch: {
    filter: {
      deep: true,
      handler() {
        this.filterChanged.next();
      },
    },
  },
  methods: {
    formatDate(value) {
      return value.format(moment.HTML5_FMT.DATETIME_LOCAL);
    },
    parseDate(value) {
      return moment(value, moment.HTML5_FMT.DATETIME_LOCAL, true);
    },
    async fetchAuditevents() {
      this.isLoading = true;
      const response = await this.instance.fetchAuditevents(this.filter);
      const converted = response.data.events.map(
        (event) => new Auditevent(event)
      );
      converted.reverse();
      this.isLoading = false;
      return converted;
    },
    createSubscription() {
      const vm = this;
      vm.filterChanged = new Subject();
      vm.error = null;

      return timer(0, 5000)
        .pipe(
          mergeWith(
            vm.filterChanged.pipe(
              debounceTime(250),
              tap({
                next: () => (vm.events = []),
              })
            )
          ),
          concatMap(this.fetchAuditevents)
        )
        .subscribe({
          next: (events) => {
            vm.addEvents(events);
          },
          error: (error) => {
            console.warn('Fetching audit events failed:', error);
            vm.error = error;
          },
        });
    },
    addEvents(events) {
      this.events = uniqBy(
        this.events ? events.concat(this.events) : events,
        (event) => event.key
      );
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/auditevents',
      parent: 'instances',
      path: 'auditevents',
      component: this,
      label: 'instances.auditevents.label',
      group: VIEW_GROUP.SECURITY,
      order: 600,
      isEnabled: ({ instance }) => instance.hasEndpoint('auditevents'),
    });
  },
};
</script>
