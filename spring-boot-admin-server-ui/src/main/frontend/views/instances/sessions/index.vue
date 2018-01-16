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
            <div class="content">
                <div class="field has-addons">
                    <div class="control">
                        <span class="select">
                          <select v-model="filterType">
                            <option value="username">Username</option>
                            <option value="sessionId">SessionId</option>
                          </select>
                        </span>
                    </div>
                    <div class="control is-expanded">
                        <input class="input" type="text" v-model="filter" @keyup.enter="fetchSessions()">
                    </div>
                </div>
            </div>
            <div class="content" :class="{ 'is-loading' : isLoading }">
                <sba-sessions-list :instance="instance" :sessions="sessions"
                                   @deleted="fetchSessions()"></sba-sessions-list>
            </div>
        </div>
    </section>
</template>

<script>
  import _ from 'lodash';
  import moment from 'moment';
  import sbaSessionsList from './sessions-list';

  const regexUuid = /[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/;

  class Session {
    constructor({id, attributeNames, creationTime, lastAccessedTime, maxInactiveInterval, expired}) {
      this.id = id;
      this.attributeNames = attributeNames;
      this.creationTime = moment(creationTime);
      this.lastAccessedTime = moment(lastAccessedTime);
      this.maxInactiveInterval = maxInactiveInterval;
      this.expired = expired;
    }
  }

  export default {
    props: ['instance', 'sessionId'],
    components: {sbaSessionsList},
    data: () => ({
      filter: '',
      filterType: 'username',
      sessions: [],
      isLoading: false
    }),
    methods: {
      fetchSessions: _.debounce(async function () {
        if (!this.filter) {
          this.sessions = [];
          return;
        }

        this.isLoading = true;
        try {
          if (this.filterType === 'username') {
            const response = await this.instance.fetchSessions(this.filter);
            this.sessions = response.data.sessions.map(session => new Session(session));
          } else {
            try {
              const response = await this.instance.fetchSession(this.filter);
              this.sessions = [new Session(response.data)];
            } catch (error) {
              if (error.response.status === 404) {
                this.sessions = [];
              } else {
                throw error;
              }
            }
          }
        } finally {
          this.isLoading = false;
        }
      }, 250),
      updateFilterfilter() {
        if (this.sessionId) {
          this.filterType = 'sessionId';
          this.filter = this.sessionId;
        }
      },
      updateRoute() {
        if (this.filterType === 'username') {
          if (this.sessionId) {
            this.$router.replace({
              name: 'instance/sessions',
              params: {'instanceId': this.instance.id}
            });
          }
        } else {
          if (this.sessionId !== this.filter) {
            this.$router.replace({
              name: 'instance/sessions',
              params: {'instanceId': this.instance.id, sessionId: this.filter}
            });
          }
        }
      }
    },
    mounted() {
      this.updateFilterfilter()
    },
    watch: {
      sessionId() {
        this.updateFilterfilter();
      },
      filterType() {
        this.updateRoute();
        this.fetchSessions();
      },
      filter() {
        const looksLikeSessionId = this.filter.match(regexUuid);
        if (looksLikeSessionId && this.filterType !== 'sessionId') {
          this.filterType = 'sessionId';
        } else if (!looksLikeSessionId && this.filterType !== 'username') {
          this.filterType = 'username';
        }
        this.updateRoute();
        this.fetchSessions();
      }
    }
  }
</script>