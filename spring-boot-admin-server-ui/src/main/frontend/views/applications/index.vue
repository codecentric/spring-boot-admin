<!--
  - Copyright 2014-2024 the original author or authors.
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
          <sba-confirm-button class="mr-1" @click="refreshContext">
            <font-awesome-icon :icon="'rotate-left'" />
          </sba-confirm-button>
          <ApplicationNotificationCenter
            v-if="hasNotificationFiltersSupport"
            :notification-filters="notificationFilters"
            @filter-remove="removeFilter"
          />
          <div class="flex-1">
            <sba-input
              v-model="routerState.q"
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
              :id="group.name"
              :key="group.name"
              v-on-clickaway="(event: Event) => deselect(event, group.name)"
              :seamless="true"
              :title="t(group.name)"
              :subtitle="
                t('term.instances_tc', { count: group.instances?.length ?? 0 })
              "
              class="application-group"
              :aria-expanded="isExpanded(group.name)"
              @title-click="
                () => {
                  select(group.name);
                  toggleGroup(group.name);
                }
              "
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
                    applicationStore.findApplicationByInstanceId(
                      group.instances[0].id,
                    )?.status
                  "
                />
              </template>

              <template v-if="singleVersionInGroup(group)" #version>
                <span v-text="group.instances[0].buildVersion" />
              </template>

              <template v-if="isGroupedByApplication" #actions>
                <ApplicationListItemAction
                  :has-notification-filters-support="
                    hasNotificationFiltersSupport
                  "
                  :item="
                    applicationStore.findApplicationByInstanceId(
                      group.instances[0].id,
                    )
                  "
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

<script lang="ts" setup>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { useNotificationCenter } from '@stekoe/vue-toast-notificationcenter';
import { groupBy, sortBy, transform } from 'lodash-es';
import { computed, nextTick, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import SbaStickySubnav from '@/components/sba-sticky-subnav.vue';
import SbaWave from '@/components/sba-wave.vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import Application from '@/services/application';
import Instance from '@/services/instance';
import NotificationFilter from '@/services/notification-filter';
import { anyValueMatches } from '@/utils/collections';
import { concatMap, mergeWith, Subject, timer } from '@/utils/rxjs';
import { useRouterState } from '@/utils/useRouterState';
import { useSubscription } from '@/utils/useSubscription';
import ApplicationListItemAction from '@/views/applications/ApplicationListItemAction.vue';
import ApplicationNotificationCenter from '@/views/applications/ApplicationNotificationCenter.vue';
import ApplicationStats from '@/views/applications/ApplicationStats.vue';
import ApplicationStatusHero from '@/views/applications/ApplicationStatusHero.vue';
import InstancesList from '@/views/applications/InstancesList.vue';
import NotificationFilterSettings from '@/views/applications/NotificationFilterSettings.vue';
import SbaButton from '@/components/sba-button.vue';
import SbaConfirmButton from '@/components/sba-confirm-button.vue';
import axios from '@/utils/axios';

const props = defineProps({
  error: {
    type: Error,
    default: null,
  },
  selected: {
    type: String,
    default: null,
  },
});

const instanceMatchesFilter = (term: string, instance: Instance) => {
  const predicate = (value: string | number) =>
    String(value).toLowerCase().includes(term);

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

const { t } = useI18n();
const router = useRouter();
const route = useRoute();
const { applications, applicationsInitialized, applicationStore } =
  useApplicationStore();
const notificationCenter = useNotificationCenter({});
const expandedGroups = ref([props.selected]);
const groupingFunction = ref(groupingFunctions.application);

const routerState = useRouterState({
  q: '',
});
const hasActiveFilter = computed(() => {
  return routerState.q?.length > 0;
});
const notificationFilterSubject = new Subject();
const hasNotificationFiltersSupport = NotificationFilter.isSupported();
const notificationFilters = ref([]);

useSubscription(
  timer(0, 60000)
    .pipe(
      mergeWith(notificationFilterSubject),
      concatMap(fetchNotificationFilters),
    )
    .subscribe({
      next: (data) => {
        notificationFilters.value = data;
      },
      error: (error) => {
        console.warn('Fetching notification filters failed with error:', error);
        notificationCenter.error(
          t('applications.fetching_notification_filters_failed'),
        );
      },
    }),
);

async function fetchNotificationFilters() {
  if (hasNotificationFiltersSupport) {
    const response = await NotificationFilter.getFilters();
    return response.data;
  }
  return [];
}

const groupNames = computed(() => {
  return [
    ...new Set(
      applications.value
        .flatMap((application: Application) => application.instances)
        .map(
          (instance: Instance) =>
            instance.registration.metadata?.['group'] ?? 'Ungrouped',
        ),
    ),
  ];
});

const grouped = computed(() => {
  const filteredApplications = filterInstances(applications.value);

  const instances = filteredApplications.flatMap(
    (application: Application) => application.instances,
  );

  const grouped = groupBy<Instance>(instances, groupingFunction.value);

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

  return sortBy(list, [(item) => getApplicationStatus(item)]);
});

const refreshContext = () => {
  axios.post('/applications').then(() => {
    notificationCenter.success(t('applications.refreshed'));
  });
};

function getApplicationStatus(item: InstancesListType): string {
  return applicationStore.findApplicationByInstanceId(item.instances[0].id)
    ?.status;
}

function filterInstances(applications: Application[]) {
  if (!routerState.q) {
    return applications;
  }

  return applications
    .map((application) =>
      application.filterInstances((i) =>
        instanceMatchesFilter(routerState.q.toLowerCase(), i),
      ),
    )
    .filter((application) => application.instances.length > 0);
}

const isGroupedByApplication = computed(() => {
  return groupingFunction.value === groupingFunctions.application;
});

const singleVersionInGroup = (group) => {
  return (
    group.length === 1 ||
    group.instances.filter(
      (instance) => group.instances[0].buildVersion !== instance.buildVersion,
    ).length === 0
  );
};

if (props.selected) {
  scrollIntoView(props.selected);
}

async function scrollIntoView(id) {
  if (id) {
    await nextTick();
    const el = document.getElementById(id);
    if (el) {
      el.scrollIntoView({
        behavior: 'smooth',
        block: 'end',
        inline: 'nearest',
      });
    }
  }
}

const showNotificationFilterSettingsObject = ref(
  null as unknown as NotificationFilterSettingsObject,
);
const setGroupingFunction = (key: keyof typeof groupingFunctions) => {
  groupingFunction.value = groupingFunctions[key];
  expandedGroups.value = [];
};

function isExpanded(name: string) {
  return expandedGroups.value.includes(name);
}

function toggleGroup(name: string) {
  if (expandedGroups.value.includes(name)) {
    expandedGroups.value = expandedGroups.value.filter((n) => n !== name);
  } else {
    expandedGroups.value = [...expandedGroups.value, name];
  }
}

function select(name: string) {
  router.push({
    name: 'applications',
    params: { selected: name },
    query: { ...route.query },
  });
}

function deselect(event: Event, expectedSelected: string) {
  if (event && event.target instanceof HTMLAnchorElement) {
    return;
  }
  toggleNotificationFilterSettings(null);
  if (!expectedSelected || props.selected === expectedSelected) {
    router.push({ name: 'applications' });
  }
}

async function addFilter({ object, ttl }) {
  try {
    const response = await NotificationFilter.addFilter(object, ttl);
    let notificationFilter = response.data;
    notificationFilterSubject.next(notificationFilter);
    notificationCenter.success(
      `${t('applications.notifications_suppressed_for', {
        name:
          notificationFilter.applicationName || notificationFilter.instanceId,
      })} <strong>${notificationFilter.expiry.fromNow(true)}</strong>.`,
    );
  } catch (error) {
    console.warn('Adding notification filter failed:', error);
  } finally {
    toggleNotificationFilterSettings(null);
  }
}

async function removeFilter(activeFilter) {
  try {
    await activeFilter.delete();
    notificationFilterSubject.next(activeFilter.id);
    notificationCenter.success(t('applications.notification_filter.removed'));
  } catch (error) {
    console.warn('Deleting notification filter failed:', error);
  } finally {
    toggleNotificationFilterSettings(null);
  }
}

function toggleNotificationFilterSettings(obj) {
  showNotificationFilterSettingsObject.value = obj ? obj : null;
}
</script>

<script lang="ts">
import { defineComponent } from 'vue';
import { directive as onClickaway } from 'vue3-click-away';

import Popper from '@/directives/popper';
import handle from '@/views/applications/handle.vue';

export default defineComponent({
  directives: { Popper, onClickaway },
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
