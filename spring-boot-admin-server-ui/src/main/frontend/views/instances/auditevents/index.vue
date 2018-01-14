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
    <section class="section" :class="{ 'is-loading' : !events}">
        <div class="container" v-if="events">
            <div class="content" v-if="events.length > 0">
                <auditevents-list :events="events"></auditevents-list>
            </div>
            <div class="content" v-else>
                <p class="is-muted">No audit events recorded.</p>
            </div>
        </div>
    </section>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import {Observable} from '@/utils/rxjs';
  import AuditeventsList from '@/views/instances/auditevents/auditevents-list';
  import moment from 'moment';

  class Auditevent {
    constructor({timestamp, type, principal, data}) {
      this.timestamp = moment(timestamp);
      this.type = type;
      this.principal = principal;
      this.data = data;
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
      return this.type.toLowerCase().indexOf('success') >= 0;
    }

    isFailure() {
      return this.type.toLowerCase().indexOf('failure') >= 0;
    }
  }

  export default {
    props: ['instance'],
    mixins: [subscribing],
    components: {AuditeventsList},
    data: () => ({
      events: null,
    }),
    computed: {},
    watch: {
      instance() {
        this.subscribe();
      },
    },
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
        if (this.instance) {
          return Observable.timer(0, 5000)
            .concatMap(this.fetchAuditevents)
            .subscribe({
              next: events => {
                vm.events = vm.events ? events.concat(vm.events) : events;
              },
              errors: err => {
                vm.unsubscribe();
              }
            });
        }
      }
    }
  }
</script>