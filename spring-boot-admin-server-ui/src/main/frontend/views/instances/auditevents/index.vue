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
      <auditevents-list v-if="events" :instance="instance" :events="events"/>
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
      events: null,
      isOldAuditevents: false
    }),
    methods: {
      async fetchAuditevents() {
        const response = await this.instance.fetchAuditevents(this.lastTimestamp);
        const converted = response.data.events.map(event => new Auditevent(event));
        converted.reverse();
        if (converted.length > 0) {
          this.lastTimestamp = converted[0].timestamp;
        }
        return converted;
      },
      createSubscription() {
        const vm = this;
        vm.lastTimestamp = moment(0);
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
