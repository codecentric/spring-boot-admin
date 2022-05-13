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
  <sba-wave />
  <section>
    <sba-sticky-subnav v-if="applications.length > 0">
      <div class="container mx-auto flex">
        <applications-stats :applications="applications" />
        <div class="flex-1">
          <sba-input
            v-model="filter"
            name="filter"
            type="search"
            :placeholder="$t('term.filter')"
          >
            <template #prepend>
              <font-awesome-icon icon="filter" />
            </template>
          </sba-input>
        </div>
      </div>
    </sba-sticky-subnav>

    <div class="container mx-auto py-6">
      <sba-alert
        v-if="error"
        :error="error"
        :title="$t('applications.server_connection_failed')"
        severity="WARN"
        class-names="mb-6"
      />
      <sba-panel v-if="!applicationsInitialized || (applicationsInitialized && applications.length === 0)">
        <p
          v-if="!applicationsInitialized"
          class="is-muted is-loading"
          v-text="$t('applications.loading_applications')"
        />
        <div
          v-if="applicationsInitialized && applications.length === 0"
          class="flex flex-col items-center"
        >
          <font-awesome-icon
            icon="frown-open"
            class="text-gray-500 text-9xl pb-4"
          />
          <h1
            class="font-bold text-2xl"
            v-text="$t('applications.no_applications_registered')"
          />
        </div>
      </sba-panel>

      <template v-if="applicationsInitialized">
        <application-status-hero
          v-if="applications.length > 0"
          :applications="applications"
        />

        <sba-panel
          v-for="group in statusGroups"
          :key="group.status"
          :seamless="true"
          class="application-group"
          :title="$tc('term.applications_tc', group.applications.length)"
        >
          <template #title>
            <sba-status-badge :status="group.statusKey" />
          </template>

          <applications-list-item
            v-for="application in group.applications"
            :key="application.name"
            :application="application"
            :has-notification-filters-support="hasNotificationFiltersSupport"
            :is-expanded="selected === application.name || Boolean(filter)"
            :notification-filters="notificationFilters"
            @unregister="unregister"
            @shutdown="shutdown"
            @restart="restart"
            @deselect="deselect"
            @select="select"
            @toggle-notification-filter-settings="toggleNotificationFilterSettings"
          />
        </sba-panel>

        <notification-filter-settings
          v-if="showNotificationFilterSettingsObject"
          v-popper="`nf-settings-${showNotificationFilterSettingsObject.id || showNotificationFilterSettingsObject.name}`"
          :notification-filters="notificationFilters"
          :object="showNotificationFilterSettingsObject"
          @filter-added="handleNotificationFilterChange"
          @filter-deleted="handleNotificationFilterChange"
        />
      </template>
    </div>
  </section>
</template>

<script>
import Popper from '@/directives/popper';
import subscribing from '../../mixins/subscribing.js';
import NotificationFilter from '@/services/notification-filter';
import {anyValueMatches} from '@/utils/collections';
import {concatMap, mergeWith, Subject, timer} from '@/utils/rxjs';
import {groupBy, sortBy, transform} from 'lodash-es';
import ApplicationsListItem from './applications-list-item.vue';
import ApplicationsStats from './applications-stats.vue';
import handle from './handle.vue';
import NotificationFilterSettings from './notification-filter-settings.vue';
import ApplicationStatusHero from '@/views/applications/application-status-hero.vue';
import SbaStickySubnav from "../../components/sba-sticky-subnav.vue";
import SbaWave from "../../components/sba-wave.vue";

const instanceMatchesFilter = (term, instance) => {
  const predicate = value => String(value).toLowerCase().includes(term);
  return anyValueMatches(instance.registration, predicate) ||
    anyValueMatches(instance.buildVersion, predicate) ||
    anyValueMatches(instance.id, predicate) ||
    anyValueMatches(instance.tags, predicate);
};

export default {
  directives: {Popper},
  components: {
    SbaWave,
    ApplicationStatusHero,
    SbaStickySubnav,
    ApplicationsStats,
    ApplicationsListItem,
    NotificationFilterSettings
  },
  mixins: [subscribing],
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
  data: () => ({
    filter: null,
    hasNotificationFiltersSupport: false,
    showNotificationFilterSettingsObject: null,
    notificationFilters: [],
    errors: [],
    palette: {}
  }),
  computed: {
    statusGroups() {
      const filteredApplications = this.filterInstances(this.applications);
      const applicationsByStatus = groupBy(filteredApplications, application => application.status);
      const list = transform(applicationsByStatus, (result, applications, status) => {
        const statusKey = status.replace(/[^\w]/gi, '').toLowerCase();
        result.push({statusKey, status: status, applications: sortBy(applications, [application => application.name])})
      }, []);
      return sortBy(list, [item => item.status]);
    }
  },
  watch: {
    '$route.query': {
      immediate: true,
      handler() {
        this.filter = this.$route.query.q || '';
      }
    },
    'selected': {
      immediate: true,
      handler(newVal) {
        this.scrollIntoView(newVal);
      }
    }
  },
  mounted() {
    this.hasNotificationFiltersSupport = NotificationFilter.isSupported();
  },
  methods: {
    select(name) {
      this.$router.replace({name: 'applications', params: {selected: name}});
    },
    deselect(event, expectedSelected) {
      if (event && event.target instanceof HTMLAnchorElement) {
        return;
      }
      this.toggleNotificationFilterSettings(null);
      if (this.selected === expectedSelected || !expectedSelected) {
        this.$router.replace({name: 'applications'});
      }
    },
    handleFilterInput(event) {
      this.$router.replace({
        name: 'applications',
        query: event.target.value ? {q: event.target.value} : null
      });
    },
    async scrollIntoView(id, behavior) {
      if (id) {
        await this.$nextTick();
        const el = document.getElementById(id);
        if (el) {
          const top = el.getBoundingClientRect().top + window.scrollY - 100;
          window.scroll({top, left: window.scrollX, behavior: behavior || 'smooth'});
        }
      }
    },
    async unregister(item) {
      this.toggleNotificationFilterSettings(null);
      try {
        item.unregister();
      } catch (e) {
        this.errors.push(e);
      }
    },
    shutdown(item) {
      try {
        item.shutdown();
      } catch (e) {
        this.errors.push(e);
      }
    },
    restart(item) {
      try {
        item.restart();
      } catch (e) {
        this.errors.push(e);
      }
    },
    createSubscription() {
      const vm = this;
      vm.notificationFilterSubject = new Subject();
      return timer(0, 60000)
        .pipe(
          mergeWith(vm.notificationFilterSubject),
          concatMap(this.fetchNotificationFilters),
        )
        .subscribe({
          next: data => {
            vm.notificationFilters = data;
          },
          error: error => {
            console.warn('Fetching notification filters failed:', error);
            vm.errors.push(error);
          }
        });
    },
    async fetchNotificationFilters() {
      if (this.hasNotificationFiltersSupport) {
        const response = await NotificationFilter.getFilters();
        return response.data;
      }
      return [];
    },
    handleNotificationFilterChange(event) {
      this.toggleNotificationFilterSettings(null);
      this.notificationFilterSubject.next(event);
    },
    toggleNotificationFilterSettings(obj) {
      this.showNotificationFilterSettingsObject = obj ? obj : null;
    },
    filterInstances(applications) {
      if (!this.filter) {
        return applications;
      }

      return applications
        .map(a => a.filterInstances(i => instanceMatchesFilter(this.filter.toLowerCase(), i)))
        .filter(a => a.instances.length > 0);
    }
  },
  install({viewRegistry}) {
    viewRegistry.addView({
      path: '/applications/:selected?',
      props: true,
      name: 'applications',
      label: 'applications.title',
      handle,
      order: 0,
      component: this
    });
    viewRegistry.addRedirect('/', 'applications');
  }
};
</script>
