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
            <input class="input" type="search" placeholder="Principal" v-model="filter.principal">
          </p>
        </div>
        <div class="field">
          <p class="control is-expanded">
            <input class="input" type="search" placeholder="Type" v-model="filter.type">
          </p>
        </div>
        <div class="field">
          <p class="control is-expanded">
            <input class="input" type="datetime-local" placeholder="Date" v-model="lastDatetime">
          </p>
        </div>
      </div>
    </div>
    <template>
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"/>
            Fetching audit events failed.
          </strong>
          <p v-text="error.message"/>
        </div>
      </div>
      <div v-if="isOldAuditevents" class="message is-warning">
        <div class="message-body">
          Audit Log is not supported for Spring Boot 1.x applications.
        </div>
      </div>
      <auditevents-list :instance="instance" :events="events" :has-loaded="hasLoaded"/>
    </template>
  </section>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import Instance from '@/services/instance';
  import {concatMap, timer} from '@/utils/rxjs';
  import AuditeventsList from '@/views/instances/auditevents/auditevents-list';
  import _ from 'lodash';
  import moment from 'moment';

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
      hasLoaded: false,
      error: null,
      events: [],
      lastDatetime: moment(0).format(moment.HTML5_FMT.DATETIME_LOCAL),
      filter: {
        lastTimestamp: moment(0),
        type: null,
        principal: null
      },
      isOldAuditevents: false
    }),
    watch: {
      filter: {
        deep: true,
        handler() {
          this.updateFilter();
        }
      },
      lastDatetime: {
        handler() {
          this.filter.lastTimestamp = moment(this.lastDatetime, moment.HTML5_FMT.DATETIME_LOCAL, true);
        }
      }
    },
    methods: {
      async fetchAuditevents() {
        const response = await this.instance.fetchAuditevents(this.filter);
        const converted = response.data.events.map(event => new Auditevent(event));
        converted.reverse();
        return converted;
      },
      updateFilter: _.debounce(function() {
        this.events = [];
        this.hasLoaded = false;
        this.unsubscribe();
        this.subscribe();
      }, 250),
      createSubscription() {
        const vm = this;
        vm.error = null;
        return timer(0, 5000)
          .pipe(
            concatMap(this.fetchAuditevents)
          )
          .subscribe({
            next: events => {
              vm.hasLoaded = true;
              vm.addEvents(events);
            },
            error: error => {
              vm.hasLoaded = true;
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
        this.events = _.uniqBy(this.events ? events.concat(this.events) : events, event => event.key);
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/auditevents',
        parent: 'instances',
        path: 'auditevents',
        component: this,
        label: 'Audit Log',
        group: 'Security',
        order: 600,
        isEnabled: ({instance}) => instance.hasEndpoint('auditevents')
      });
    }
  }
</script>
