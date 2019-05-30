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
  <section class="section">
    <div class="field has-addons">
      <div class="control">
        <span class="select">
          <select v-model="filter.type">
            <option value="username" v-text="$t('term.username')" />
            <option value="sessionId" v-text="$t('instances.sessions.session_id')" />
          </select>
        </span>
      </div>
      <div class="control is-expanded">
        <input class="input" type="text" v-model="filter.value"
               @keyup.enter="fetchSessionsByUsername()" @paste="handlePaste"
        >
      </div>
    </div>
    <div v-if="error" class="message is-danger">
      <div class="message-body">
        <strong>
          <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
          <span v-text="$t('instances.sessions.fetch_failed')" />
        </strong>
        <p v-text="error.message" />
      </div>
    </div>
    <sba-sessions-list :instance="instance" :sessions="sessions" :is-loading="isLoading"
                       @deleted="fetch"
    />
  </section>
</template>

<script>
  import Instance from '@/services/instance';
  import debounce from 'lodash/debounce';
  import isEqual from 'lodash/isEqual';
  import moment from 'moment'
  import sbaSessionsList from './sessions-list'
  import {VIEW_GROUP} from '../../index';

  const regexUuid = /[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/;

  class Session {
    constructor({creationTime, lastAccessedTime, ...session}) {
      Object.assign(this, session);
      this.creationTime = moment(creationTime);
      this.lastAccessedTime = moment(lastAccessedTime)
    }
  }

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    components: {sbaSessionsList},
    data: () => ({
      error: null,
      filter: {value: '', type: null},
      sessions: [],
      isLoading: false
    }),
    methods: {
      fetch: debounce(async function () {
        this.error = null;
        if (!this.filter.value) {
          this.sessions = [];
          return;
        }

        this.isLoading = true;
        try {
          if (this.filter.type === 'sessionId') {
            this.sessions = await this.fetchSession()
          } else {
            this.sessions = await this.fetchSessionsByUsername()
          }
        } catch (error) {
          console.warn('Fetching sessions failed:', error);
          this.error = error
        }
        this.isLoading = false
      }, 250),
      async fetchSession() {
        try {
          const response = await this.instance.fetchSession(this.filter.value);
          return [new Session(response.data)]
        } catch (error) {
          if (error.response.status === 404) {
            return []
          } else {
            throw error
          }
        }
      },
      async fetchSessionsByUsername() {
        const response = await this.instance.fetchSessionsByUsername(this.filter.value);
        return response.data.sessions.map(session => new Session(session))
      },
      handlePaste(event) {
        const looksLikeSessionId = event.clipboardData.getData('text').match(regexUuid);
        if (looksLikeSessionId) {
          this.filter.type = 'sessionId';
        }
      }
    },
    watch: {
      '$route.query': {
        immediate: true,
        handler() {
          this.filter = Object.entries(this.$route.query)
            .reduce((acc, [name, value]) => {
              acc.type = name;
              acc.value = value;
              return acc;
            }, {type: 'username', value: ''});
        }
      },
      filter: {
        deep: true,
        immediate: true,
        handler() {
          const oldQuery = {[this.filter.type]: this.filter.value};
          if (!isEqual(oldQuery, this.$route.query)) {
            this.$router.replace({
              name: 'instances/sessions',
              query: oldQuery
            });
          }
          this.fetch();
        }
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/sessions',
        parent: 'instances',
        path: 'sessions',
        component: this,
        label: 'instances.sessions.label',
        group: VIEW_GROUP.SECURITY,
        order: 700,
        isEnabled: ({instance}) => instance.hasEndpoint('sessions')
      });
    }
  }
</script>
