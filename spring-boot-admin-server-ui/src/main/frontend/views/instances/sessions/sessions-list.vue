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
  <table class="sessions table w-full">
    <thead>
      <tr>
        <th v-html="$t('instances.sessions.session_id')" />
        <th v-html="$t('instances.sessions.created_at')" />
        <th v-html="$t('instances.sessions.last_accessed_at')" />
        <th v-html="$t('instances.sessions.expiry')" />
        <th v-html="$t('instances.sessions.max_inactive_interval')" />
        <th v-html="$t('instances.sessions.attributes')" />
        <th>
          <sba-confirm-button
            v-if="sessions.length > 1"
            :class="{
              'is-loading': deletingAll === 'executing',
              'is-info': deletingAll === 'completed',
              'is-danger': deletingAll === 'failed',
            }"
            :disabled="deletingAll !== null"
            class="button"
            @click="deleteAllSessions()"
          >
            <span
              v-if="deletingAll === 'completed'"
              v-text="$t('term.deleted')"
            />
            <span
              v-else-if="deletingAll === 'failed'"
              v-text="$t('term.failed')"
            />
            <span v-else>
              <font-awesome-icon icon="trash" />&nbsp;
              <span v-text="$t('term.delete')" />
            </span>
          </sba-confirm-button>
        </th>
      </tr>
    </thead>
    <tr v-for="session in sessions" :key="session.id">
      <td>
        <router-link
          :to="{
            name: 'instances/sessions',
            params: { instanceId: instance.id },
            query: { sessionId: session.id },
          }"
        >
          {{ session.id }}
        </router-link>
      </td>
      <td v-text="session.creationTime.format('L HH:mm:ss.SSS')" />
      <td v-text="session.lastAccessedTime.format('L HH:mm:ss.SSS')" />
      <td>
        <span
          v-if="session.expired"
          class="tag is-info"
          v-text="$t('instances.sessions.expired')"
        />
      </td>
      <td>
        <span
          v-if="session.maxInactiveInterval >= 0"
          v-text="`${session.maxInactiveInterval}s`"
        />
        <span v-else v-text="$t('instances.sessions.unlimited')" />
      </td>
      <td>
        <span
          v-for="name in session.attributeNames"
          :key="`${session.id}-${name}`"
          class="tag"
          v-text="name"
        />
      </td>
      <td>
        <button
          :class="{
            'is-loading': deleting[session.id] === 'executing',
            'is-info': deleting[session.id] === 'completed',
            'is-danger': deleting[session.id] === 'failed',
          }"
          :disabled="session.id in deleting"
          class="button"
          @click="deleteSession(session.id)"
        >
          <span
            v-if="deleting[session.id] === 'completed'"
            v-text="$t('term.deleted')"
          />
          <span
            v-else-if="deleting[session.id] === 'failed'"
            v-text="$t('term.failed')"
          />
          <span v-else>
            <font-awesome-icon icon="trash" />&nbsp;
            <span v-text="$t('term.delete')" />
          </span>
        </button>
      </td>
    </tr>
    <tr v-if="sessions.length === 0">
      <td class="is-muted" colspan="7">
        <p
          v-if="isLoading"
          class="is-loading"
          v-text="$t('instances.sessions.loading_sessions')"
        />
        <p v-else v-text="$t('instances.sessions.no_sessions_found')" />
      </td>
    </tr>
  </table>
</template>

<script>
import prettyBytes from 'pretty-bytes';

import Instance from '@/services/instance';
import { concatMap, from, listen, map, of, tap } from '@/utils/rxjs';

export default {
  props: {
    sessions: {
      type: Array,
      default: () => [],
    },
    instance: {
      type: Instance,
      required: true,
    },
    isLoading: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['deleted'],
  data: () => ({
    deletingAll: null,
    deleting: {},
  }),
  methods: {
    prettyBytes,
    deleteAllSessions() {
      this.subscription = from(this.sessions)
        .pipe(
          map((session) => session.id),
          concatMap(this._deleteSession),
          listen((status) => (this.deletingAll = status)),
        )
        .subscribe({
          complete: () => {
            this.$emit('deleted', '*');
          },
        });
    },
    deleteSession(sessionId) {
      this._deleteSession(sessionId)
        .pipe(listen((status) => (this.deleting[sessionId] = status)))
        .subscribe({
          complete: () => this.$emit('deleted', sessionId),
        });
    },
    _deleteSession(sessionId) {
      return of(sessionId).pipe(
        concatMap(async (sessionId) => {
          await this.instance.deleteSession(sessionId);
          return sessionId;
        }),
        tap({
          error: (error) => {
            console.warn(`Deleting session ${sessionId} failed:`, error);
          },
        }),
      );
    },
  },
};
</script>
<style lang="css">
.sessions td,
.sessions th {
  vertical-align: middle;
}
</style>
