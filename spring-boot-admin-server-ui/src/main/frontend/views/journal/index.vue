<!--
  - Copyright 2014-2019 the original author or authors.
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
  <div class="section">
    <div class="container">
      <h1 class="title" v-text="$t('journal.title')" />
      <h2
        v-if="filter.application"
        v-text="filter.application"
        class="subtitle"
      />
      <h1
        v-else-if="filter.instanceId"
        v-text="`${getName(filter.instanceId)} (${filter.instanceId})`"
        class="subtitle"
      />
      <div v-if="error" class="message is-warning">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-warning" icon="exclamation-triangle" />
            <span v-text="$t('error.server_connection_failed')" />
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <table class="journal table is-fullwidth is-hoverable">
        <thead>
          <tr>
            <th v-text="$t('term.application')" />
            <th v-text="$t('term.instances')" />
            <th v-text="$t('term.time')" />
            <th v-text="$t('term.event')" />
          </tr>
        </thead>
        <transition-group tag="tbody" name="fade-in">
          <tr key="new-events" v-if="newEventsCount > 0">
            <td
              colspan="4"
              class="has-text-primary has-text-centered is-selectable"
              v-text="`${newEventsCount} new events`"
              @click="showNewEvents"
            />
          </tr>
          <template v-for="event in listedEvents">
            <tr class="is-selectable" :key="event.key"
                @click="showPayload[event.key] ? $delete(showPayload, event.key) : $set(showPayload, event.key, true)"
            >
              <td v-text="getName(event.instance)" />
              <td v-text="event.instance" />
              <td v-text="event.timestamp.format('L HH:mm:ss.SSS')" />
              <td>
                <span v-text="event.type" /> <span v-if="event.type === 'STATUS_CHANGED'"
                                                   v-text="`(${event.payload.statusInfo.status})`"
                />
              </td>
            </tr>
            <tr :key="`${event.key}-detail`" v-if="showPayload[event.key]">
              <td colspan="4">
                <pre class="is-breakable" v-text="toJson(event.payload)" />
              </td>
            </tr>
          </template>
        </transition-group>
      </table>
    </div>
  </div>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import Instance from '@/services/instance';
  import {compareBy} from '@/utils/collections';
  import isEqual from 'lodash/isEqual';
  import uniq from 'lodash/uniq';
  import moment from 'moment';

  class Event {
    constructor({instance, version, type, timestamp, ...payload}) {
      this.instance = instance;
      this.version = version;
      this.type = type;
      this.timestamp = moment(timestamp);
      this.payload = payload;
    }

    get key() {
      return `${this.instance}-${this.version}`;
    }
  }

  export default {
    mixins: [subscribing],
    data: () => ({
      events: [],
      listOffset: 0,
      showPayload: {},
      error: null,
      filter: {
        application: undefined,
        instanceId: undefined
      }
    }),
    computed: {
      instanceNames() {
        return this.events.filter(event => event.type === 'REGISTERED').reduce((names, event) => {
          names[event.instance] = event.payload.registration.name;
          return names;
        }, {});
      },
      listedEvents() {
        return this.filterEvents(this.events.slice(this.listOffset));
      },
      newEventsCount() {
        return this.filterEvents(this.events.slice(0, this.listOffset)).length;
      }
    },
    methods: {
      toJson(obj) {
        return JSON.stringify(obj, null, 4);
      },
      getName(instanceId) {
        return this.instanceNames[instanceId] || '?'
      },
      getInstances(application) {
        return uniq(Object.entries(this.instanceNames)
          .filter(([, name]) => application === name)
          .map(([instanceId]) => instanceId));
      },
      showNewEvents() {
        this.listOffset = 0;
      },
      filterEvents(events) {
        if (this.filter.application) {
          const instances = this.getInstances(this.filter.application);
          return events.filter(e => instances.includes(e.instance))
        }
        if (this.filter.instanceId) {
          return events.filter(e => e.instance === this.filter.instanceId);
        }
        return events;
      },
      createSubscription() {
        return Instance.getEventStream().subscribe({
          next: message => {
            this.error = null;
            this.events = Object.freeze([new Event(message.data), ...this.events]);
            this.listOffset += 1;
          },
          error: error => {
            console.warn('Listening for events failed:', error);
            this.error = error;
          }
        });
      }
    },
    watch: {
      '$route.query': {
        immediate: true,
        handler() {
          this.filter = this.$route.query
        }
      },
      filter: {
        deep: true,
        immediate: true,
        handler() {
          if (!isEqual(this.filter, this.$route.query)) {
            this.$router.replace({
              name: 'journal',
              query: this.filter
            });
          }
        }
      }
    },
    async created() {
      try {
        const response = await Instance.fetchEvents();
        const events = response.data.sort(compareBy(v => v.timestamp)).reverse().map(e => new Event(e));
        this.events = Object.freeze(events);
        this.error = null;
      } catch (error) {
        console.warn('Fetching events failed:', error);
        this.error = error;
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        path: '/journal',
        name: 'journal',
        label: 'journal.label',
        order: 100,
        component: this
      });
    }
  };
</script>
