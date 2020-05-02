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
  <section class="wallboard section">
    <p v-if="!applicationsInitialized" class="is-muted is-loading">
      Loading applications...
    </p>

    <div class="field is-grouped-right">
      <sba-icon-button icon="filter" icon-class="filter-icon-color" @click="toggleFilter" />
    </div>

    <div class="modal" :class="displayFilter ? 'is-active' : ''">
      <div class="modal-background" />
      <div class="modal-card">
        <div class="modal-background" />
        <div class="modal-content">
          <div class="field">
            <p class="control is-expanded has-icons-left">
              <input
                id="filter-wallboard"
                class="input"
                type="search"
                :value="filter"
                @input="handleFilterInput"
                @keyup.enter="toggleFilter"
              >
              <span class="icon is-small is-left">
                <font-awesome-icon icon="filter" />
              </span>
            </p>
          </div>
          <button class="modal-close is-large" aria-label="close" @click="toggleFilter" />
        </div>
      </div>
    </div>


    <hex-mesh v-if="applicationsInitialized"
              :items="filteredApplications"
              :class-for-item="classForApplication"
              @click="select"
    >
      <div class="hex__body application" slot="item" slot-scope="{item: application}" :key="application.name">
        <div class="application__header application__time-ago is-muted">
          <sba-time-ago :date="application.statusTimestamp" />
        </div>
        <div class="application__body">
          <h1 class="application__name" v-text="application.name" />
          <p class="application__instances is-muted"
             v-text="$tc('wallboard.instances_count', application.instances.length)"
          />
        </div>
        <h2 class="application__footer application__version" v-text="application.buildVersion" />
      </div>
    </hex-mesh>
  </section>
</template>

<script>
  import hexMesh from './hex-mesh';
  import SbaIconButton from '../../components/sba-icon-button';
  import {instanceMatchesFilter} from '@/services/application';

  export default {
    data: () => ({
      filter: null,
      displayFilter: false
    }),
    components: {SbaIconButton, hexMesh},
    computed: {
      filteredApplications() {
        if (!this.applications) {
          return [];
        }
        if (!this.filter) {
          return this.applications;
        }
        return this.applications
          .map(a => a.filterInstances(i => instanceMatchesFilter(this.filter.toLowerCase(), i)))
          .filter(a => a.instances.length > 0);
      }
    },
    watch: {
      '$route.query': {
        immediate: true,
        handler() {
          this.filter = this.$route.query.q;
        }
      },
    },
    props: {
      applications: {
        type: Array,
        default: () => []
      },
      error: {
        type: Error,
        default: null
      },
      applicationsInitialized: {
        type: Boolean,
        default: false
      }
    },
    methods: {
      classForApplication(application) {
        if (!application) {
          return null;
        }
        if (application.status === 'UP') {
          return 'is-selectable is-primary';
        }
        if (application.status === 'RESTRICTED') {
          return 'is-selectable is-warning';
        }
        if (application.status === 'DOWN') {
          return 'is-selectable is-danger';
        }
        if (application.status === 'OUT_OF_SERVICE') {
          return 'is-selectable is-danger';
        }
        if (application.status === 'OFFLINE') {
          return 'is-selectable is-light';
        }
        return 'is-selectable is-light';
      },
      select(application) {
        if (application.instances.length === 1) {
          this.$router.push({name: 'instances/details', params: {instanceId: application.instances[0].id}});
        } else {
          this.$router.push({name: 'applications', params: {selected: application.name}});
        }
      },
      handleFilterInput(event) {
        this.$router.replace({
          name: 'wallboard',
          query: event.target.value ? {q: event.target.value} : null
        });
      },
      toggleFilter() {
        this.displayFilter = !this.displayFilter;
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        path: '/wallboard',
        name: 'wallboard',
        label: 'wallboard.label',
        order: -100,
        component: this
      });
    }
  };
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";



  .wallboard {
    background-color: $grey-dark;
    height: calc(100vh - #{$navbar-height-px});
    width: 100%;

    .filter-icon-color {
      color: $black;
    }

    & .application {
      color: $white-ter;
      font-size: 1em;
      font-weight: $weight-normal;
      line-height: 1;
      text-align: center;

      overflow: hidden;
      display: flex;
      flex-direction: column;

      &__name {
        width: 100%;
        padding: 2.5%;
        color: $white;
        font-size: 2em;
        font-weight: $weight-semibold;
        line-height: 1.125;
      }

      &__version {
        color: $white-ter;
        font-size: 1.25em;
        line-height: 1.25;
      }

      &__header {
        width: 90%;
        margin-bottom: 0.5em;
      }

      &__footer {
        width: 90%;
        margin-top: 0.5em;
      }
    }
  }
</style>
