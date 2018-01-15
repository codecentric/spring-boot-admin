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
    <table class="table">
        <thead>
        <tr>
            <th></th>
            <th>Session Id</th>
            <th>Created at</th>
            <th>Last accessed at</th>
            <th>Max. inactive interval
            </th>
            <th>Attributes</th>
            <th>
                <sba-confirm-button class="button" :class="{ 'is-loading' : deletingAll }"
                                    :disabled="sessions.length <= 0" @click="deleteAllSessions()">
                    <font-awesome-icon icon="trash"></font-awesome-icon>&nbsp;Delete
                </sba-confirm-button>
            </th>
        </tr>
        </thead>
        <tr v-for="session in sessions" :key="session.id">
            <td>
                <span v-if="session.expired" class="tag is-info">Expired</span>
            </td>
            <td v-text="session.id"></td>
            <td v-text="session.creationTime.format('L HH:mm:ss.SSS')"></td>
            <td v-text="session.lastAccessedTime.format('L HH:mm:ss.SSS')"></td>
            <td>
                <span v-if="session.maxInactiveInterval >= 0" v-text="`${session.maxInactiveInterval}s`"></span>
                <span v-else>unlimited</span>
            </td>
            <td>
                <span class="tag" v-for="name in session.attributeNames" :key="`${session.id}-${name}`"
                      v-text="name"></span>
            </td>
            <td>
                <button class="button" :class="{ 'is-loading' : deletingAll ||  deleting[session.id] }"
                        @click="deleteSession(session.id)">
                    <font-awesome-icon icon="trash"></font-awesome-icon>&nbsp;Delete
                </button>
            </td>
        </tr>
        <tr v-if="sessions.length=== 0">
            <td class="is-muted" colspan="7 ">No sessions found.</td>
        </tr>
    </table>
</template>

<script>
  import {Observable} from '@/utils/rxjs';
  import prettyBytes from 'pretty-bytes';

  export default {
    props: ['sessions', 'instance'],
    data: () => ({
      deletingAll: false,
      deleting: []
    }),
    methods: {
      prettyBytes,
      async deleteAllSessions() {
        const vm = this;
        vm.deletingAll = true;
        this.subscription = Observable.from(vm.sessions)
          .map(session => session.id)
          .concatMap(async sessionId => {
            await vm.instance.deleteSession(sessionId);
            return sessionId;
          })
          .finally(() => {
            vm.deletingAll = false;
            vm.$emit('deleted');
          })
          .subscribe({
            error: (err) => {
            },
          });
      },
      async deleteSession(sessionId) {
        const vm = this;
        vm.$set(vm.deleting, sessionId, true);
        this.subscription = Observable.of(sessionId)
          .concatMap(async sessionId => {
            await vm.instance.deleteSession(sessionId);
            return sessionId;
          })
          .finally(() => {
            vm.$emit('deleted');
            vm.$delete(vm.deleting, sessionId);
          })
          .subscribe({
            error: (err) => {
            },
          });
      }
    }
  }
</script>