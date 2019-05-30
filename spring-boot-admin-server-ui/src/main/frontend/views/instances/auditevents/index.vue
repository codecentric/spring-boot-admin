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
    <div class="field is-horizontal">
      <div class="field-body">
        <div class="field">
          <p class="control is-expanded">
            <input
              class="input"
              type="search"
              :placeholder="$t('instances.auditevents.principal')"
              v-model.trim="filter.principal"
            >
          </p>
        </div>
        <div class="field">
          <p class="control is-expanded">
            <input
              list="auditevent-type"
              class="input"
              type="search"
              :placeholder="$t('instances.auditevents.type')"
              v-model="filter.type"
            >
            <datalist id="auditevent-type">
              <option value="AUTHENTICATION_FAILURE" />
              <option value="AUTHENTICATION_SUCCESS" />
              <option value="AUTHENTICATION_SWITCH" />
              <option value="AUTHORIZATION_FAILURE" />
            </datalist>
          </p>
        </div>
        <div class="field">
          <p class="control is-expanded">
            <input
              class="input"
              type="datetime-local"
              placeholder="Date"
              :value="formatDate(filter.after)"
              @input="filter.after = parseDate($event.target.value)"
            >
          </p>
        </div>
      </div>
    </div>
    <template>
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon
              class="has-text-danger"
              icon="exclamation-triangle"
            />
            <span v-text="$t('instances.auditevents.fetch_failed')" />
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <div
        v-if="isOldAuditevents"
        class="message is-warning"
      >
        <div class="message-body" v-html="$t('instances.auditevents.audit_log_not_supported_spring_boot_1')" />
      </div>
      <auditevents-list
        :instance="instance"
        :events="events"
        :is-loading="isLoading"
      />
    </template>
  </section>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import Instance from '@/services/instance';
  import {concatMap, debounceTime, merge, Subject, tap, timer} from '@/utils/rxjs';
  import AuditeventsList from '@/views/instances/auditevents/auditevents-list';
  import uniqBy from 'lodash/uniqBy';
  import moment from 'moment';
  import {VIEW_GROUP} from '../../index';

  class Auditevent {
    constructor({timestamp, ...event}) {
      Object.assign(this, event);
      this.timestamp = moment(timestamp);
    }

    get key() {
      return `${this.timestamp}-${this.type}-${this.principal}`;
    }

    get remoteAddress() {
      return this.data && this.data.details && this.data.details.remoteAddress || null;
    }

    get sessionId() {
      return this.data && this.data.details && this.data.details.sessionId || null;
    }

    isSuccess() {
      return this.type.toLowerCase().includes('success');
    }

    isFailure() {
      return this.type.toLowerCase().includes('failure');
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
    components: {AuditeventsList},
    data: () => ({
      isLoading: false,
      error: null,
      events: [],
      filter: {
        after: moment().startOf('day'),
        type: null,
        principal: null
      },
      isOldAuditevents: false
    }),
    watch: {
      filter: {
        deep: true,
        handler() {
          this.filterChanged.next();
        }
      }
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
        const converted = response.data.events.map(event => new Auditevent(event));
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
            merge(vm.filterChanged.pipe(
              debounceTime(250),
              tap({
                next: () => vm.events = []
              })
            )),
            concatMap(this.fetchAuditevents)
          )
          .subscribe({
            next: events => {
              vm.addEvents(events);
            },
            error: error => {
              console.warn('Fetching audit events failed:', error);
              if (error.response.headers['content-type'].includes('application/vnd.spring-boot.actuator.v2')) {
                vm.error = error;
              } else {
                vm.isOldAuditevents = true;
              }
            }
          });
      },
      addEvents(events) {
        this.events = uniqBy(this.events ? events.concat(this.events) : events, event => event.key);
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/auditevents',
        parent: 'instances',
        path: 'auditevents',
        component: this,
        label: 'instances.auditevents.label',
        group: VIEW_GROUP.SECURITY,
        order: 600,
        isEnabled: ({instance}) => instance.hasEndpoint('auditevents')
      });
    }
  }
</script>
