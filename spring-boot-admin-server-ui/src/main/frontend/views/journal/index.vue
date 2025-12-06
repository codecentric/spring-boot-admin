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
    </div>

    <sba-alert :error="error" />

    <JournalTable :events="events" :applications="applications" />
  </div>
</template>

<script>
import SbaAlert from '@/components/sba-alert';

import { useApplicationStore } from '@/composables/useApplicationStore';
import { useDateTimeFormatter } from '@/composables/useDateTimeFormatter';
import subscribing from '@/mixins/subscribing';
import Instance from '@/services/instance';
import { compareBy } from '@/utils/collections';
import { InstanceEvent } from '@/views/journal/InstanceEvent';
import { deduplicateInstanceEvents } from '@/views/journal/deduplicate-events';
import {
  InstanceEventType,
} from '@/views/journal/InstanceEvent';
import JournalTable from '@/views/journal/JournalTable.vue';

export default {
  components: { JournalTable, SbaAlert },
  mixins: [subscribing],
  setup() {
    const { formatDateTime } = useDateTimeFormatter();
    const { applications } = useApplicationStore();

    return {
      formatDate: formatDateTime,
      applications,
    };
  },
  data: () => ({
    Event,
    events: [],
    seenEventKeys: new Set(),
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
        .filter(
          (event) =>
            event.type === InstanceEventType.REGISTERED ||
            event.type === InstanceEventType.REGISTRATION_UPDATED,
        )
        .sort((a, b) => b.timestamp - a.timestamp)
        .reduceRight((names, event) => {
          names[event.instance] = event.payload.registration.name;
          return names;
        }, {});
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

      const deduplicated = deduplicateInstanceEvents(events);
      this.seenEventKeys = new Set(deduplicated.map((event) => event.key));
      this.events = Object.freeze(deduplicated);
      this.error = null;
    } catch (error) {
      console.warn('Fetching events failed:', error);
      this.error = error;
    }
  },
  methods: {
    getName(instanceId) {
      return this.instanceNames[instanceId] || '?';
    },
    createSubscription() {
      return Instance.getEventStream().subscribe({
        next: (message) => {
          this.error = null;
          const incomingEvent = new InstanceEvent(message.data);
          if (this.seenEventKeys.has(incomingEvent.key)) {
            return;
          }
          this.seenEventKeys.add(incomingEvent.key);
          this.events = Object.freeze([incomingEvent, ...this.events]);
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
