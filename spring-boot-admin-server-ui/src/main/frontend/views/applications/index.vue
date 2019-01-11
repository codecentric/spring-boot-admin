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
      <p v-if="!applicationsInitialized" class="is-muted is-loading">
        Loading applications...
      </p>
      <div v-if="error" class="message is-warning">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-warning" icon="exclamation-triangle" />
            Server connection failed.
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <template v-if="applicationsInitialized">
        <applications-stats :applications="applications" />
        <div class="application-group" v-for="group in statusGroups" :key="group.status">
          <p class="heading" v-text="group.status" />
          <applications-list :applications="group.applications" :selected="selected" />
        </div>
        <p v-if="statusGroups.length === 0" class="is-muted">
          No applications registered.
        </p>
      </template>
    </div>
  </section>
</template>

<script>
  import groupBy from 'lodash/groupBy';
  import sortBy from 'lodash/sortBy';
  import transform from 'lodash/transform';
  import applicationsList from './applications-list';
  import applicationsStats from './applications-stats';
  import handle from './handle';

  export default {
    props: {
      applications: {
        type: Array,
        default: () => [],
      },
      error: {
        type: Error,
        default: null
      },
      selected: {
        type: String,
        default: null
      },
      applicationsInitialized: {
        type: Boolean,
        default: false
      }
    },
    components: {applicationsStats, applicationsList},
    computed: {
      statusGroups() {
        const byStatus = groupBy(this.applications, application => application.status);
        const list = transform(byStatus, (result, value, key) => {
          result.push({status: key, applications: sortBy(value, [application => application.name])})
        }, []);
        return sortBy(list, [item => item.status]);
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        path: '/applications/:selected?',
        props: true,
        name: 'applications',
        label: 'Applications',
        handle,
        order: 0,
        component: this
      });
      viewRegistry.addRedirect('/', 'applications');
    }
  };
</script>


<style lang="scss">
  @import "~@/assets/css/utilities";

  .application-group {
    margin: $gap 0;
  }
</style>
