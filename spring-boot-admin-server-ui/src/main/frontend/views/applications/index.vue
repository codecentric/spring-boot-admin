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
  <div>
    <sba-wave />
    <section>
      <sba-sticky-subnav>
        <div class="container mx-auto flex">
          <ApplicationStats />
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

        <ApplicationStatusHero v-if="applicationsInitialized" />

        <template v-if="applicationsInitialized">
          <sba-panel
            v-if="hasActiveFilter && grouped.length === 0"
            class="text-center"
          >
            {{ t('filter.no_results') }}
          </sba-panel>

          <template v-else>
            <div v-if="groupNames.length > 1" class="text-right mb-6">
              <sba-button-group>
                <sba-button @click="() => setGroupingFunction('application')">
                  <font-awesome-icon icon="list" />
                </sba-button>
                <sba-button @click="() => setGroupingFunction('group')">
                  <font-awesome-icon icon="expand" />
                </sba-button>
              </sba-button-group>
            </div>

            <sba-panel
              v-for="group in grouped"
              :key="group.name"
              :seamless="true"
              :title="t(group.name)"
              :subtitle="
                t('term.instances_tc', { count: group.instances?.length ?? 0 })
              "
              class="application-group"
              @title-click="() => toggleGroup(group.name)"
            >
              <template #prefix>
                <font-awesome-icon
                  icon="chevron-down"
                  :class="{
                    '-rotate-90': !isExpanded(group.name),
                    'mr-2 transition-[transform]': true,
                  }"
                />
                <sba-status-badge
                  v-if="isGroupedByApplication"
                  class="mr-2"
                  :status="
                    findApplicationByInstanceId(group.instances[0].id)?.status
                  "
                />
              </template>

              <template v-if="isGroupedByApplication" #actions>
                <ApplicationListItemAction
                  :has-notification-filters-support="
                    hasNotificationFiltersSupport
                  "
                  :item="findApplicationByInstanceId(group.instances[0].id)"
                  @filter-settings="toggleNotificationFilterSettings"
                />
              </template>

              <template v-if="isExpanded(group.name)" #default>
                <InstancesList :instances="group.instances">
                  <template #actions="{ instance }">
                    <ApplicationListItemAction
                      :has-notification-filters-support="
                        hasNotificationFiltersSupport
                      "
                      :item="instance"
                      class="md:hidden"
                      @filter-settings="toggleNotificationFilterSettings"
                    />
                  </template>
                </InstancesList>
              </template>
            </sba-panel>
          </template>

          <NotificationFilterSettings
            v-if="showNotificationFilterSettingsObject"
            v-on-clickaway="() => toggleNotificationFilterSettings(null)"
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
  </div>
</template>

<script lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { useNotificationCenter } from '@stekoe/vue-toast-notificationcenter';
import { groupBy, sortBy, transform } from 'lodash-es';
import { computed, defineComponent, ref, watch } from 'vue';
import { directive as onClickaway } from 'vue3-click-away';
import { useI18n } from 'vue-i18n';
import { RouteLocationNamedRaw, useRoute, useRouter } from 'vue-router';

import SbaStickySubnav from '@/components/sba-sticky-subnav.vue';
import SbaWave from '@/components/sba-wave.vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import Popper from '@/directives/popper';
import subscribing from '@/mixins/subscribing';
import Application from '@/services/application';
import Instance from '@/services/instance';
import NotificationFilter from '@/services/notification-filter';
import { anyValueMatches } from '@/utils/collections';
import { Subject, concatMap, mergeWith, timer } from '@/utils/rxjs';
import ApplicationListItemAction from '@/views/applications/ApplicationListItemAction.vue';
import ApplicationNotificationCenter from '@/views/applications/ApplicationNotificationCenter.vue';
import ApplicationStats from '@/views/applications/ApplicationStats.vue';
import ApplicationStatusHero from '@/views/applications/ApplicationStatusHero.vue';
import InstancesList from '@/views/applications/InstancesList.vue';
import NotificationFilterSettings from '@/views/applications/NotificationFilterSettings.vue';
import handle from '@/views/applications/handle.vue';

const instanceMatchesFilter = (term, instance) => {
  const predicate = (value) => String(value).toLowerCase().includes(term);
  return (
    anyValueMatches(instance.registration, predicate) ||
    anyValueMatches(instance.buildVersion, predicate) ||
    anyValueMatches(instance.id, predicate) ||
    anyValueMatches(instance.tags, predicate)
  );
};

type NotificationFilterSettingsObject = { id: string; name: string };

type InstancesListType = {
  name?: string;
  statusKey?: string;
  status?: string;
  instances?: Instance[];
  applications?: Application[];
};

const groupingFunctions = {
  application: (instance: Instance) => instance.registration.name,
  group: (instance: Instance) =>
    instance.registration.metadata?.['group'] ?? 'term.no_group',
};

export default defineComponent({
  directives: { Popper, onClickaway },
  components: {
    FontAwesomeIcon,
    InstancesList,
    ApplicationListItemAction,
    ApplicationNotificationCenter,
    SbaWave,
    ApplicationStatusHero,
    SbaStickySubnav,
    ApplicationStats,
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
  setup: function (props) {
    const { t } = useI18n();
    const router = useRouter();
    const route = useRoute();
    const { applications, applicationsInitialized, applicationStore } =
      useApplicationStore();
    const notificationCenter = useNotificationCenter({});
    const filter = ref(route.query.q?.toString());
    const expandedGroups = ref([]);
    const groupingFunction = ref(groupingFunctions.application);

    watch(filter, (q) => {
      let to = {
        name: 'applications',
        params: { selected: props.selected },
      } as RouteLocationNamedRaw;

      if (q?.length > 0) {
        to = {
          ...to,
          query: {
            q,
          },
        } as RouteLocationNamedRaw;
      }

      router.replace(to);
    });

    const hasActiveFilter = computed(() => {
      return filter.value?.length > 0;
    });

    return {
      applications,
      applicationsInitialized,
      setGroupingFunction: (key: keyof typeof groupingFunctions) => {
        groupingFunction.value = groupingFunctions[key];
        expandedGroups.value = [];
      },
      findApplicationByInstanceId: applicationStore.findApplicationByInstanceId,
      errors: ref([]),
      filter,
      hasActiveFilter,
      hasNotificationFiltersSupport: ref(false),
      notificationCenter,
      notificationFilterSubject: new Subject(),
      notificationFilters: ref([]),
      palette: ref({}),
      router,
      expandedGroups,
      isExpanded: (name: string) => expandedGroups.value.includes(name),
      groupingFunction,
      toggleGroup: (name: string) => {
        if (expandedGroups.value.includes(name)) {
          expandedGroups.value = expandedGroups.value.filter((n) => n !== name);
        } else {
          expandedGroups.value = [...expandedGroups.value, name];
        }
      },
      showNotificationFilterSettingsObject: ref(
        null as unknown as NotificationFilterSettingsObject,
      ),
      t,
    };
  },
  computed: {
    groupNames() {
      return [
        ...new Set(
          this.applications
            .flatMap((application) => application.instances)
            .map(
              (instance) =>
                instance.registration.metadata?.['group'] ?? 'Ungrouped',
            ),
        ),
      ];
    },
    grouped() {
      const filteredApplications = this.filterInstances(this.applications);

      const instances = filteredApplications.flatMap(
        (application) => application.instances,
      );

      const grouped = groupBy<Instance>(instances, this.groupingFunction);

      const list = transform<Instance[], InstancesListType[]>(
        grouped,
        (result, instances, name) => {
          result.push({
            name,
            instances: sortBy(instances, [
              (instance) => instance.registration.name,
            ]),
          });
        },
        [],
      );

      return sortBy(list, [(item) => item.status]);
    },
    isGroupedByApplication() {
      return this.groupingFunction === groupingFunctions.application;
    },
  },
  watch: {
    selected: {
      immediate: true,
      handler: 'scrollIntoView',
    },
  },
  mounted() {
    this.hasNotificationFiltersSupport = NotificationFilter.isSupported();
  },
  methods: {
    select(name) {
      this.router.replace({
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
        this.router.replace({ name: 'applications' });
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
    createSubscription() {
      return timer(0, 60000)
        .pipe(
          mergeWith(this.notificationFilterSubject),
          concatMap(this.fetchNotificationFilters),
        )
        .subscribe({
          next: (data) => {
            this.notificationFilters = data;
          },
          error: (error) => {
            console.warn(
              'Fetching notification filters failed with error:',
              error,
            );
            this.notificationCenter.error(
              this.t('applications.fetching_notification_filters_failed'),
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
        this.notificationCenter.success(
          `${this.t('applications.notifications_suppressed_for', {
            name:
              notificationFilter.applicationName ||
              notificationFilter.instanceId,
          })} <strong>${notificationFilter.expiry.fromNow(true)}</strong>.`,
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
        this.notificationCenter.success(
          this.t('applications.notification_filter.removed'),
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
    filterInstances(applications: Application[]) {
      if (!this.filter) {
        return applications;
      }

      return applications
        .map((application) =>
          application.filterInstances((i) =>
            instanceMatchesFilter(this.filter.toLowerCase(), i),
          ),
        )
        .filter((application) => application.instances.length > 0);
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      path: '/applications/:selected?',
      props: true,
      name: 'applications',
      handle,
      order: 0,
      component: this,
    });
    viewRegistry.addRedirect('/', 'applications');
  },
});
</script>
