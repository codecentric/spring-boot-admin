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
  <div class="applications-list">
    <applicatios-list-item v-for="application in applications"
                           :key="application.name"
                           :id="application.name"
                           :application="application"
                           @click.stop="select(application.name)"
                           v-on-clickaway="(event) => deselect(event, application.name)"
                           :is-expanded="selected === application.name"
                           :has-notification-filters-support="hasNotificationFiltersSupport"
                           :notification-filters="notificationFilters"
                           @unregister="unregister"
                           @toggle-notification-filter-settings="toggleNotificationFilterSettings"
    />
    <notification-filter-settings v-if="showNotificationFilterSettingsObject"
                                  v-popper="`nf-settings-${showNotificationFilterSettingsObject.id || showNotificationFilterSettingsObject.name}`"
                                  :notification-filters="notificationFilters"
                                  :object="showNotificationFilterSettingsObject"
                                  @filter-added="handleFilterChange"
                                  @filter-deleted="handleFilterChange"
    />
  </div>
</template>
<script>
  import Popper from '@/directives/popper';
  import subscribing from '@/mixins/subscribing';
  import NotificationFilter from '@/services/notification-filter';
  import {concatMap, merge, Subject, timer} from '@/utils/rxjs';
  import {directive as onClickaway} from 'vue-clickaway';
  import ApplicationsListItem from './applications-list-item';
  import NotificationFilterSettings from './notification-filter-settings';

  export default {
    props: {
      applications: {
        type: Array,
        default: () => []
      },
      selected: {
        type: String,
        default: null
      }
    },
    directives: {onClickaway, Popper},
    mixins: [subscribing],
    components: {ApplicatiosListItem: ApplicationsListItem, NotificationFilterSettings},
    data: () => ({
      errors: [],
      hasNotificationFiltersSupport: false,
      notificationFilters: [],
      showNotificationFilterSettingsObject: null
    }),
    computed: {},
    methods: {
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
      handleFilterChange(event) {
        this.toggleNotificationFilterSettings(null);
        this.notificationFilterSubject.next(event);
      },
      toggleNotificationFilterSettings(obj) {
        this.showNotificationFilterSettingsObject = obj ? obj : null;
      }
    },
    mounted() {
      this.scrollIntoView(this.selected, 'instant');
      this.hasNotificationFiltersSupport = NotificationFilter.isSupported();
    },
    watch: {
      selected(newVal) {
        this.scrollIntoView(newVal);
      }
    }
  }
</script>

