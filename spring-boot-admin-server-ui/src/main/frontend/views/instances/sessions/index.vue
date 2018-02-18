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
  <section class="section">
    <div class="container">
      <div class="field has-addons">
        <div class="control">
          <span class="select">
            <select v-model="filter.type">
              <option value="username">Username</option>
              <option value="sessionId">SessionId</option>
            </select>
          </span>
        </div>
        <div class="control is-expanded">
          <input class="input" type="text" v-model="filter.value" @keyup.enter="fetchSessions()">
        </div>
      </div>
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"/>
            Fetching sessions failed.
          </strong>
          <p v-text="error.message"/>
        </div>
      </div>
      <sba-sessions-list :instance="instance" :sessions="sessions"
                         @deleted="fetch"/>
    </div>
  </section>
</template>

<script>
  import Instance from '@/services/instance';
  import _ from 'lodash'
  import moment from 'moment'
  import sbaSessionsList from './sessions-list'

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
      fetch: _.debounce(async function () {
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
            this.sessions = await this.fetchSessions()
          }
        } catch (error) {
          console.warn('Fetching sessions failed:', error);
          this.error = error
        } finally {
          this.isLoading = false
        }
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
      async fetchSessions() {
        const response = await this.instance.fetchSessions(this.filter.value);
        return response.data.sessions.map(session => new Session(session))
      },
      updateFilter() {
        this.filter = _.entries(this.$route.query)
          .reduce((acc, [name, value]) => {
            acc.type = name;
            acc.value = value;
            return acc;
          }, {type: null, value: ''});
      }
    },
    mounted() {
      this.updateFilter();
    },
    watch: {
      '$route.query'() {
        this.updateFilter();
      },
      filter: {
        deep: true,
        handler() {
          if (this.filter.type === null) {
            const looksLikeSessionId = this.filter.value.match(regexUuid);
            this.filter.type = looksLikeSessionId ? 'sessionId' : 'username';
          }

          const query = {[this.filter.type]: this.filter.value};
          if (!_.isEqual(query, !this.$route.query)) {
            this.$router.replace({
              name: 'instance/sessions',
              query: query
            });
          }

          this.fetch();
        }
      }
    }
  }
</script>
