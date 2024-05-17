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
  <div class="px-12 pt-10 pb-6">
    <div class="flex mb-6">
      <div class="flex-1">
        <h1 class="title" v-text="$t('journal.title')" />
        <h2
          v-if="filter.application"
          class="subtitle"
          v-text="filter.application"
        />
        <h1
          v-else-if="filter.instanceId"
          class="subtitle"
          v-text="`${getName(filter.instanceId)} (${filter.instanceId})`"
        />
      </div>
      <div>
        <label class="label" v-text="$t('journal.per_page.per_page')" />
        <sba-select
          v-model="pageSize"
          :options="[
            { value: 10, label: 10 },
            { value: 25, label: 25 },
            { value: 50, label: 50 },
            { value: 100, label: 100 },
            { value: 200, label: 200 },
            { value: 500, label: 500 },
            { value: events.length, label: $t('journal.per_page.all') },
          ]"
          name="pageSize"
          @change="setPageSize($event.target.value)"
        />
      </div>
    </div>

    <sba-alert :error="error" />
    {{ error }}

    <sba-panel :seamless="true">
      <table class="table table-full table-striped">
        <thead>
          <tr>
            <th v-text="$t('term.application')" />
            <th v-text="$t('term.instance')" />
            <th v-text="$t('term.time')" />
            <th v-text="$t('term.event')" />
          </tr>
        </thead>
        <tbody />
        <transition>
          <tr v-if="newEventsCount > 0">
            <td
              class="has-text-primary has-text-centered is-selectable"
              colspan="4"
              @click="showNewEvents"
              v-text="`${newEventsCount} new events`"
            />
          </tr>
        </transition>
        <transition-group name="fade-in" tag="tbody">
          <template v-for="event in listedEvents" :key="event.key">
            <tr
              class="cursor-pointer"
              @click="
                showPayload[event.key]
                  ? delete showPayload[event.key]
                  : (showPayload[event.key] = true)
              "
            >
              <td class="flex items-center">
                <font-awesome-icon
                  :class="{ 'rotate-90': showPayload[event.key] === true }"
                  :icon="['fas', 'chevron-right']"
                  class="mr-2 transition-all"
                />
                <span v-text="getName(event.instance)" />
              </td>
              <td>
                <router-link
                  :to="{
                    name: 'instances/details',
                    params: { instanceId: event.instance },
                  }"
                >
                  {{ event.instance }}
                </router-link>
              </td>
              <td v-text="event.timestamp.format('L HH:mm:ss.SSS')" />
              <td>
                <span v-text="event.type" />
                <span
                  v-if="event.type === Event.STATUS_CHANGED"
                  v-text="`(${event.payload.statusInfo.status})`"
                />
              </td>
            </tr>
            <tr v-if="showPayload[event.key]" :key="`${event.key}-detail`">
              <td colspan="4">
                <pre
                  class="whitespace-pre-wrap text-sm"
                  v-text="toJson(event.payload)"
                />
              </td>
            </tr>
          </template>
        </transition-group>
      </table>
    </sba-panel>

    <sba-pagination-nav
      v-model="current"
      :page-count="pageCount"
      :page-size="pageSize"
      class="mt-6 text-center"
    />
  </div>
</template>

<script>
import { isEqual, uniq } from 'lodash-es';
import moment from 'moment';

import SbaAlert from '@/components/sba-alert';

import subscribing from '@/mixins/subscribing';
import Instance from '@/services/instance';
import { compareBy } from '@/utils/collections';

class InstanceEvent {
  constructor({ instance, version, type, timestamp, ...payload }) {
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
InstanceEvent.STATUS_CHANGED = 'STATUS_CHANGED';
InstanceEvent.REGISTERED = 'REGISTERED';
InstanceEvent.DEREGISTERED = 'DEREGISTERED';
InstanceEvent.REGISTRATION_UPDATED = 'REGISTRATION_UPDATED';
InstanceEvent.INFO_CHANGED = 'INFO_CHANGED';
InstanceEvent.ENDPOINTS_DETECTED = 'ENDPOINTS_DETECTED';

export default {
  components: { SbaAlert },
  mixins: [subscribing],
  data: () => ({
    Event,
    events: [],
    listOffset: 0,
    showPayload: {},
    pageSize: 25,
    current: 1,
    error: null,
    filter: {
      application: undefined,
      instanceId: undefined,
    },
  }),
  computed: {
    instanceNames() {
      return this.events
        .filter((event) => event.type === InstanceEvent.REGISTERED)
        .reduce((names, event) => {
          names[event.instance] = event.payload.registration.name;
          return names;
        }, {});
    },
    listedEvents() {
      return this.filterEvents(this.events).slice(
        this.indexStart,
        this.indexEnd,
      );
    },
    newEventsCount() {
      return this.filterEvents(this.events.slice(0, this.listOffset)).length;
    },
    indexStart() {
      return (this.current - 1) * +this.pageSize;
    },
    pageCount() {
      return Math.ceil(this.filterEvents(this.events).length / +this.pageSize);
    },
    indexEnd() {
      return this.indexStart + +this.pageSize;
    },
  },
  watch: {
    '$route.query': {
      immediate: true,
      handler() {
        this.filter = this.$route.query;
      },
    },
    filter: {
      deep: true,
      immediate: true,
      handler() {
        if (!isEqual(this.filter, this.$route.query)) {
          this.$router.replace({
            name: 'journal',
            query: this.filter,
          });
        }
      },
    },
  },
  async created() {
    try {
      const response = await Instance.fetchEvents();
      const events = response.data
        .map((e, idx) => ({
          ...e,
          version: idx,
        }))
        .sort(compareBy((v) => v.timestamp))
        .reverse()
        .map((e) => new InstanceEvent(e));

      this.events = Object.freeze(events);
      this.error = null;
    } catch (error) {
      console.warn('Fetching events failed:', error);
      this.error = error;
    }
  },
  methods: {
    setPageSize(newPageSize) {
      this.current = 1;
      this.pageSize = +newPageSize;
    },
    toJson(obj) {
      return JSON.stringify(obj, null, 4);
    },
    getName(instanceId) {
      return this.instanceNames[instanceId] || '?';
    },
    getInstances(application) {
      return uniq(
        Object.entries(this.instanceNames)
          .filter(([, name]) => application === name)
          .map(([instanceId]) => instanceId),
      );
    },
    showNewEvents() {
      this.listOffset = 0;
    },
    filterEvents(events) {
      if (this.filter.application) {
        const instances = this.getInstances(this.filter.application);
        return events.filter((e) => instances.includes(e.instance));
      }
      if (this.filter.instanceId) {
        return events.filter((e) => e.instance === this.filter.instanceId);
      }
      return events;
    },
    createSubscription() {
      return Instance.getEventStream().subscribe({
        next: (message) => {
          this.error = null;
          this.events = Object.freeze([
            new InstanceEvent(message.data),
            ...this.events,
          ]);
          this.listOffset += 1;
        },
        error: (error) => {
          console.warn('Listening for events failed:', error);
          this.error = error;
        },
      });
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      path: '/journal',
      name: 'journal',
      label: 'journal.label',
      order: 100,
      component: this,
    });
  },
};
</script>

<style scoped>
.label {
  white-space: nowrap;
}

.title {
  @apply text-3xl;
}

.subtitle {
  @apply text-xl;
}
</style>
