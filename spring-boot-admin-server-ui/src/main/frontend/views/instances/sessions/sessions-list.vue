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
    <table class="table is-fullwidth">
        <thead>
        <tr>
            <th></th>
            <th>Session Id</th>
            <th>Created at</th>
            <th>Last accessed at</th>
            <th>Max. inactive<br>interval
            </th>
            <th>Attributes</th>
            <th>
                <sba-confirm-button class="button"
                                    :class="{ 'is-loading' : deletingAll === 'deleting', 'is-danger' : deletingAll === 'failed' }"
                                    :disabled="deletingAll !== null"
                                    v-if="sessions.length > 1" @click="deleteAllSessions()">
                    <span v-if="deletingAll === 'deleted'">Deleted</span>
                    <span v-else-if="deletingAll === 'failed'">Failed</span>
                    <span v-else><font-awesome-icon icon="trash"></font-awesome-icon>&nbsp;Delete</span>
                </sba-confirm-button>
            </th>
        </tr>
        </thead>
        <tr v-for="session in sessions" :key="session.id">
            <td>
                <span v-if="session.expired" class="tag is-info">Expired</span>
            </td>
            <td>
                <router-link v-text="session.id"
                             :to="{ name: 'instance/sessions', params: { 'instanceId' : instance.id, sessionId : session.id } }">
                </router-link>
            </td>
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
                <button class="button"
                        :class="{ 'is-loading' :  deleting[session.id] === 'deleting', 'is-info' : deleting[session.id] === 'deleted', 'is-danger' : deleting[session.id] === 'failed' }"
                        :disabled="session.id in deleting" @click="deleteSession(session.id)">
                    <span v-if="deleting[session.id] === 'deleted'">Deleted</span>
                    <span v-else-if="deleting[session.id] === 'failed'">Failed</span>
                    <span v-else><font-awesome-icon icon="trash"></font-awesome-icon>&nbsp;Delete</span>
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
      deletingAll: null,
      deleting: {},
    }),
    methods: {
      prettyBytes,
      deleteAllSessions() {
        const vm = this;
        vm.deletingAll = 'deleting';
        this.subscription = Observable.from(vm.sessions)
          .map(session => session.id)
          .concatMap(vm._deleteSession)
          .subscribe({
            complete: () => {
              vm.deletingAll = 'deleted';
              vm.$emit('deleted', '*');
            },
            error: error => {
              vm.deletingAll = 'failed';
            },
          });
      },
      deleteSession(sessionId) {
        const vm = this;
        vm._deleteSession(sessionId)
          .subscribe({
            complete: () => vm.$emit('deleted', sessionId),
          });
      },
      _deleteSession(sessionId) {
        const vm = this;
        vm.$set(vm.deleting, sessionId, 'deleting');
        return Observable.of(sessionId)
          .concatMap(async sessionId => {
            await vm.instance.deleteSession(sessionId);
            return sessionId;
          })
          .do({
            next: sessionId => vm.$set(vm.deleting, sessionId, 'deleted'),
            error: error => {
              console.warn(`Deleting session ${sessionId} failed:`, error);
              vm.$set(vm.deleting, sessionId, 'failed');
            }
          });
      }
    }
  }
</script>