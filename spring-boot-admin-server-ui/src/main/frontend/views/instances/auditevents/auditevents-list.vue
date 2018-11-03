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
  <table class="auditevents table is-hoverable is-fullwidth">
    <thead>
      <tr>
        <th>Timestamp</th>
        <th>Event</th>
        <th>Principal</th>
        <th>Remote address</th>
        <th>Session Id</th>
      </tr>
    </thead>
    <tbody>
      <template v-for="event in events">
        <tr class="is-selectable"
            :class="{ 'auditevents__event--is-detailed' : showDetails[event.key] }"
            @click="showDetails[event.key] ? $delete(showDetails, event.key) : $set(showDetails, event.key, true)"
            :key="event.key"
        >
          <td v-text="event.timestamp.format('L HH:mm:ss.SSS')" />
          <td>
            <span v-text="event.type" class="tag"
                  :class="{ 'is-success' : event.isSuccess(), 'is-danger' : event.isFailure() }"
            />
          </td>
          <td v-if="hasSessionEndpoint && event.principal">
            <router-link v-text="event.principal"
                         :to="{ name: 'instances/sessions', params: { 'instanceId' : instance.id }, query: { username : event.principal} }"
            />
          </td>
          <td v-else v-text="event.principal" />
          <td v-text="event.remoteAddress" />
          <td v-if="hasSessionEndpoint && event.sessionId">
            <router-link v-text="event.sessionId"
                         :to="{ name: 'instances/sessions', params: { 'instanceId' : instance.id }, query: { sessionId : event.sessionId } }"
            />
          </td>
          <td v-else v-text="event.sessionId" />
        </tr>
        <tr :key="`${event.key}-detail`" v-if="showDetails[event.key]">
          <td colspan="5">
            <pre class="auditevents__event-detail" v-text="toJson(event.data)" />
          </td>
        </tr>
      </template>
      <tr v-if="events.length === 0">
        <td class="is-muted" colspan="5">
          <p v-if="isLoading" class="is-loading">Loading Audit Events...</p>
          <p v-else>No Audit Events found.</p>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script>
  import Instance from '@/services/instance';
  import prettyBytes from 'pretty-bytes';

  export default {
    props: {
      events: {
        type: Array,
        default: () => []
      },
      instance: {
        type: Instance,
        required: true
      },
      isLoading: {
        type: Boolean,
        default: false
      }
    },
    data: () => ({
      showDetails: {}
    }),
    computed: {
      hasSessionEndpoint() {
        return this.instance.hasEndpoint('sessions');
      }
    },
    methods: {
      prettyBytes,
      toJson(obj) {
        return JSON.stringify(obj, null, 4);
      }
    }
  }
</script>

<style lang="scss">
  .auditevents {
    table-layout: fixed;

    td {
      vertical-align: middle;
    }

    &__event--is-detailed td {
      border: none !important;
    }

    &__event-detail {
      overflow: auto;
    }
  }
</style>
