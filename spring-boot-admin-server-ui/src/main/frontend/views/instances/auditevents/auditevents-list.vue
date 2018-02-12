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
    <table class="table is-hoverable is-fullwidth">
        <thead>
        <tr>
            <th>Timestamp</th>
            <th>Event</th>
            <th>Principal</th>
            <th>Remote address</th>
            <th>Session Id</th>
        </tr>
        </thead>
        <template v-for="event in events">
            <tr class="is-selectable"
                :class="{ 'event--is-detailed' : showDetails[event.key] }"
                @click="showDetails[event.key]  ?  $delete(showDetails, event.key) : $set(showDetails, event.key, true)"
                :key="event.key">
                <td v-text="event.timestamp.format('L HH:mm:ss.SSS')"></td>
                <td>
                    <span v-text="event.type" class="tag"
                          :class="{ 'is-success' : event.isSuccess(),  'is-danger' : event.isFailure() }"></span>
                </td>
                <td v-text="event.principal"></td>
                <td v-text="event.remoteAddress"></td>
                <td v-if="hasSessionEndpoint && event.sessionId">
                    <router-link v-text="event.sessionId"
                                 :to="{ name: 'instance/sessions', params: { 'instanceId' : instance.id, sessionId : event.sessionId } }">
                    </router-link>
                </td>
                <td v-else v-text="event.sessionId"></td>
            </tr>
            <tr class="event__detail" :key="`${event.key}-detail`" v-if="showDetails[event.key]">
                <td colspan="5">
                    <pre v-text="toJson(event.data)"></pre>
                </td>
            </tr>
        </template>
        <tr v-if="events.length === 0">
            <td class="is-muted" colspan="5">No auditevents found.</td>
        </tr>
    </table>
</template>

<script>
  import prettyBytes from 'pretty-bytes';

  export default {
    props: ['events', 'instance'],
    data: () => ({
      showDetails: {}
    }),
    computed: {
      hasSessionEndpoint() {
        return this.instance && this.instance.hasEndpoint('sessions');
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
    @import "~@/assets/css/utilities";

    .event--is-detailed td {
        border: none !important;
    }

    .event__detail td {
        overflow-x: auto;
        max-width: 1024px;
    }
</style>