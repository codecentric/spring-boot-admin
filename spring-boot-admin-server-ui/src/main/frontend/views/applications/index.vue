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
    <div class="container">
      <p v-if="!applicationsInitialized" class="is-muted is-loading" v-text="$t('applications.loading_applications')" />
      <div v-if="error" class="message is-warning">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-warning" icon="exclamation-triangle" />
            <span v-text="$t('applications.server_connection_failed')" />
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <template v-if="applicationsInitialized">
        <applications-stats :applications="applications" />

        <div class="field">
          <p class="control is-expanded has-icons-left">
            <input
              class="input"
              type="search"
              :value="filter"
              @input="handleFilterInput"
            >
            <span class="icon is-small is-left">
              <font-awesome-icon icon="filter" />
            </span>
          </p>
        </div>
        <div
          class="application-group"
          v-for="group in statusGroups"
          :key="group.status"
        >
          <p class="heading" v-text="$t('applications.' + group.statusKey)" />
          <div class="applications-list">
            <applications-list-item v-for="application in group.applications"
                                    :key="application.name"
                                    :id="application.name"
                                    :application="application"
                                    :is-expanded="selected === application.name || Boolean(filter)"
                                    :has-notification-filters-support="hasNotificationFiltersSupport"
                                    :notification-filters="notificationFilters"
                                    @click.stop="select(application.name)"
                                    v-on-clickaway="(event) => deselect(event, application.name)"
                                    @unregister="unregister"
                                    @toggle-notification-filter-settings="toggleNotificationFilterSettings"
            />
          </div>
        </div>
        <p v-if="applications.length === 0" class="is-muted" v-text="$t('applications.no_applications_registered')" />
        <notification-filter-settings v-if="showNotificationFilterSettingsObject"
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
  import subscribing from '@/mixins/subscribing';
  import NotificationFilter from '@/services/notification-filter';
  import {anyValueMatches} from '@/utils/collections';
  import {concatMap, merge, Subject, timer} from '@/utils/rxjs';
  import groupBy from 'lodash/groupBy';
  import sortBy from 'lodash/sortBy';
  import transform from 'lodash/transform';
  import {directive as onClickaway} from 'vue-clickaway2';
  import ApplicationsListItem from './applications-list-item';
  import applicationsStats from './applications-stats';
  import handle from './handle';
  import NotificationFilterSettings from './notification-filter-settings';

  const instanceMatchesFilter = (term, instance) => {
    const predicate = value => String(value).toLowerCase().includes(term);
    return anyValueMatches(instance.registration, predicate) ||
      anyValueMatches(instance.buildVersion, predicate) ||
      anyValueMatches(instance.id, predicate) ||
      anyValueMatches(instance.tags, predicate);
  };

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
    directives: {onClickaway, Popper},
    mixins: [subscribing],
    components: {
      applicationsStats,
      ApplicationsListItem,
      NotificationFilterSettings
    },
    data: () => ({
      filter: null,
      hasNotificationFiltersSupport: false,
      showNotificationFilterSettingsObject: null,
      notificationFilters: []
    }),
    watch: {
      '$route.query': {
        immediate: true,
        handler() {
          this.filter = this.$route.query.q;
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
      handleFilterInput(event) {
        this.$router.replace({
          name: 'applications',
          query: event.target.value ? {q: event.target.value} : null
        });
      },
      select(name) {
        this.$router.replace({name: 'applications', params: {selected: name}});
      },
      deselect(event, expectedSelected) {
        this.toggleNotificationFilterSettings(null);
        if (event && event.target instanceof HTMLAnchorElement) {
          return;
        }
        if (this.selected === expectedSelected || !expectedSelected) {
          this.$router.replace({name: 'applications'});
        }
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
      createSubscription() {
        const vm = this;
        vm.notificationFilterSubject = new Subject();
        return timer(0, 60000)
          .pipe(
            merge(vm.notificationFilterSubject),
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
