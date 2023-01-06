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
    <sba-sticky-subnav>
      <div class="container mx-auto flex">
        <application-stats />
        <ApplicationNotificationCenter
          v-if="hasNotificationFiltersSupport"
          :notification-filters="notificationFilters"
          @filter-remove="removeFilter"
        />
        <div class="flex-1">
          <sba-input
            v-model="filter"
            :placeholder="t('term.filter')"
            name="filter"
            type="search"
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
        :title="t('applications.server_connection_failed')"
        class-names="mb-6"
        severity="WARN"
      />
      <sba-panel v-if="!applicationsInitialized">
        <p
          class="is-muted is-loading"
          v-text="t('applications.loading_applications')"
        />
      </sba-panel>

      <application-status-hero v-if="applicationsInitialized" />

      <template v-if="applicationsInitialized">
        <TransitionGroup>
          <sba-panel
            v-for="group in statusGroups"
            :key="group.status"
            :seamless="true"
            :title="t('term.applications_tc', group.applications.length)"
            class="application-group"
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
              @deselect="deselect"
              @restart="restart"
              @select="select"
              @shutdown="shutdown"
              @unregister="unregister"
              @toggle-notification-filter-settings="
                toggleNotificationFilterSettings
              "
            />
          </sba-panel>
        </TransitionGroup>
        <notification-filter-settings
          v-if="showNotificationFilterSettingsObject"
          v-popper="
            `nf-settings-${
              showNotificationFilterSettingsObject.id ||
              showNotificationFilterSettingsObject.name
            }`
          "
          :notification-filters="notificationFilters"
          :object="showNotificationFilterSettingsObject"
          @filter-add="addFilter"
          @filter-remove="removeFilter"
        />
      </template>
    </div>
  </section>
</template>

<script>
import { groupBy, sortBy, transform } from 'lodash-es';
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';

import SbaStickySubnav from '@/components/sba-sticky-subnav';
import SbaWave from '@/components/sba-wave';

import { useApplicationStore } from '@/composables/useApplicationStore';
import Popper from '@/directives/popper';
import subscribing from '@/mixins/subscribing';
import Application from '@/services/application';
import NotificationFilter from '@/services/notification-filter';
import { anyValueMatches } from '@/utils/collections';
import { Subject, concatMap, mergeWith, timer } from '@/utils/rxjs';
import ApplicationNotificationCenter from '@/views/applications/application-notification-center';
import ApplicationStats from '@/views/applications/application-stats';
import ApplicationStatusHero from '@/views/applications/application-status-hero';
import ApplicationsListItem from '@/views/applications/applications-list-item';
import handle from '@/views/applications/handle';
import NotificationFilterSettings from '@/views/applications/notification-filter-settings';

const instanceMatchesFilter = (term, instance) => {
  const predicate = (value) => String(value).toLowerCase().includes(term);
  return (
    anyValueMatches(instance.registration, predicate) ||
    anyValueMatches(instance.buildVersion, predicate) ||
    anyValueMatches(instance.id, predicate) ||
    anyValueMatches(instance.tags, predicate)
  );
};

export default {
  directives: { Popper },
  components: {
    ApplicationNotificationCenter,
    SbaWave,
    ApplicationStatusHero,
    SbaStickySubnav,
    ApplicationStats,
    ApplicationsListItem,
    NotificationFilterSettings,
  },
  mixins: [subscribing],
  props: {
    error: {
      type: Error,
      default: null,
    },
    selected: {
      type: String,
      default: null,
    },
  },
  setup: function () {
    const { t } = useI18n();
    const { applications, applicationsInitialized } = useApplicationStore();

    return {
      applications,
      applicationsInitialized,
      t,
      filter: ref(null),
      hasNotificationFiltersSupport: ref(false),
      showNotificationFilterSettingsObject: ref(null),
      notificationFilters: ref([]),
      errors: ref([]),
      palette: ref({}),
    };
  },
  computed: {
    statusGroups() {
      const filteredApplications = this.filterInstances(this.applications);
      const applicationsByStatus = groupBy(
        filteredApplications,
        (application) => application.status
      );
      const list = transform(
        applicationsByStatus,
        (result, applications, status) => {
          const statusKey = status.replace(/[^\w]/gi, '').toLowerCase();
          result.push({
            statusKey,
            status: status,
            applications: sortBy(applications, [
              (application) => application.name,
            ]),
          });
        },
        []
      );
      return sortBy(list, [(item) => item.status]);
    },
  },
  watch: {
    '$route.query': {
      immediate: true,
      handler() {
        this.filter = this.$route.query.q || '';
      },
    },
    selected: {
      immediate: true,
      handler(newVal) {
        this.scrollIntoView(newVal);
      },
    },
  },
  mounted() {
    this.hasNotificationFiltersSupport = NotificationFilter.isSupported();
  },
  methods: {
    select(name) {
      this.$router.replace({
        name: 'applications',
        params: { selected: name },
      });
    },
    deselect(event, expectedSelected) {
      if (event && event.target instanceof HTMLAnchorElement) {
        return;
      }
      this.toggleNotificationFilterSettings(null);
      if (this.selected === expectedSelected || !expectedSelected) {
        this.$router.replace({ name: 'applications' });
      }
    },
    async scrollIntoView(id, behavior) {
      if (id) {
        await this.$nextTick();
        const el = document.getElementById(id);
        if (el) {
          const top = el.getBoundingClientRect().top + window.scrollY - 100;
          window.scroll({
            top,
            left: window.scrollX,
            behavior: behavior || 'smooth',
          });
        }
      }
    },
    async unregister(item) {
      this.toggleNotificationFilterSettings(null);
      try {
        await item.unregister();
        const message =
          item instanceof Application
            ? 'applications.unregister_successful'
            : 'instances.unregister_successful';
        this.$notificationCenter.success(
          this.t(message, { name: item.id || item.name })
        );
      } catch (error) {
        const message =
          item instanceof Application
            ? 'applications.unregister_failed'
            : 'instances.unregister_failed';
        this.$notificationCenter.error(
          this.t(message, {
            name: item.id || item.name,
            error: error.response.status,
          })
        );
      }
    },
    async shutdown(item) {
      try {
        await item.shutdown();
        const message =
          item instanceof Application
            ? 'applications.shutdown_successful'
            : 'instances.shutdown_successful';
        this.$notificationCenter.success(
          this.t(message, { name: item.id || item.name })
        );
      } catch (error) {
        const message =
          item instanceof Application
            ? 'applications.shutdown_failed'
            : 'instances.shutdown_failed';
        this.$notificationCenter.error(
          this.t(message, {
            name: item.id || item.name,
            error: error.response.status,
          })
        );
      }
    },
    async restart(item) {
      try {
        await item.restart();
        const message =
          item instanceof Application
            ? 'applications.restarted'
            : 'instances.restarted';
        this.$notificationCenter.success(
          this.t(message, { name: item.id || item.name })
        );
      } catch (error) {
        const message =
          item instanceof Application
            ? 'applications.restart_failed'
            : 'instances.restart_failed';
        this.$notificationCenter.error(
          this.t(message, {
            name: item.id || item.name,
            error: error.response.status,
          })
        );
      }
    },
    createSubscription() {
      const vm = this;
      vm.notificationFilterSubject = new Subject();
      return timer(0, 60000)
        .pipe(
          mergeWith(vm.notificationFilterSubject),
          concatMap(this.fetchNotificationFilters)
        )
        .subscribe({
          next: (data) => {
            vm.notificationFilters = data;
          },
          error: (error) => {
            console.warn(
              'Fetching notification filters failed with error:',
              error
            );
            this.$notificationCenter.error(
              this.t('applications.fetching_notification_filters_failed')
            );
          },
        });
    },
    async fetchNotificationFilters() {
      if (this.hasNotificationFiltersSupport) {
        const response = await NotificationFilter.getFilters();
        return response.data;
      }
      return [];
    },
    async addFilter({ object, ttl }) {
      try {
        const response = await NotificationFilter.addFilter(object, ttl);
        let notificationFilter = response.data;
        this.notificationFilterSubject.next(notificationFilter);
        this.$notificationCenter.success(
          `${this.t('applications.notifications_suppressed_for', {
            name:
              notificationFilter.applicationName ||
              notificationFilter.instanceId,
          })} <strong>${notificationFilter.expiry.fromNow(true)}</strong>.`
        );
      } catch (error) {
        console.warn('Adding notification filter failed:', error);
      } finally {
        this.toggleNotificationFilterSettings(null);
      }
    },
    async removeFilter(activeFilter) {
      try {
        await activeFilter.delete();
        this.notificationFilterSubject.next(activeFilter.id);
        this.$notificationCenter.success(
          this.t('applications.notification_filter.removed')
        );
      } catch (error) {
        console.warn('Deleting notification filter failed:', error);
      } finally {
        this.toggleNotificationFilterSettings(null);
      }
    },
    toggleNotificationFilterSettings(obj) {
      this.showNotificationFilterSettingsObject = obj ? obj : null;
    },
    filterInstances(applications) {
      if (!this.filter) {
        return applications;
      }

      return applications
        .map((application) =>
          application.filterInstances((i) =>
            instanceMatchesFilter(this.filter.toLowerCase(), i)
          )
        )
        .filter((application) => application.instances.length > 0);
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      path: '/applications/:selected?',
      props: true,
      name: 'applications',
      label: 'applications.title',
      handle,
      order: 0,
      component: this,
    });
    viewRegistry.addRedirect('/', 'applications');
  },
};
</script>
