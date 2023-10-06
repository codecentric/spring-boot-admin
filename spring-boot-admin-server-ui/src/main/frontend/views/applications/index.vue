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
    <sba-wave/>
    <section>
      <sba-sticky-subnav>
        <div class="container mx-auto flex">
          <ApplicationStats/>
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
                <font-awesome-icon icon="filter"/>
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

        <ApplicationStatusHero v-if="applicationsInitialized"/>

        <template v-if="applicationsInitialized">
          <sba-panel
              v-if="hasActiveFilter && grouped.length === 0"
              class="text-center"
          >
            {{ t('filter.no_results') }}
          </sba-panel>

          <template v-else>
            <div class="text-right mb-6" v-if="groupNames.length > 1">
              <sba-button-group>
                <sba-button @click="() => groupingCriterion = 'application'">
                  <font-awesome-icon icon="list"/>
                </sba-button>
                <sba-button @click="() => groupingCriterion = 'group'">
                  <font-awesome-icon icon="expand"/>
                </sba-button>
              </sba-button-group>
            </div>

            <sba-panel
                v-for="group in grouped"
                :key="group.name"
                :seamless="true"
                :id="group.name"
                :title="t(group.name)"
                :subtitle="t('term.instances_tc', {count: group.instances?.length ?? 0})"
                class="application-group"
                @titleClick="() => titleClick(group.name)"
            >
              <template #prefix>
                <font-awesome-icon icon="chevron-down"
                                   :class="{'-rotate-90': !isExpanded(group.name), 'mr-2 transition-[transform]': true}"/>
                <sba-status-badge class="mr-2" v-if="isGroupedByApplication"
                                  :status="applicationStore.findApplicationByInstanceId(group.instances[0].id)?.status"/>
              </template>

              <template #actions v-if="isGroupedByApplication">
                <ApplicationListItemAction
                    :has-notification-filters-support="hasNotificationFiltersSupport"
                    :item="applicationStore.findApplicationByInstanceId(group.instances[0].id)"
                    @filter-settings="toggleNotificationFilterSettings"
                />
              </template>

              <template #default v-if="isExpanded(group.name)">
                <InstancesList :instances="group.instances">
                  <template #actions="{instance}">
                    <ApplicationListItemAction
                        :has-notification-filters-support="hasNotificationFiltersSupport"
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
              v-click-away="() => toggleNotificationFilterSettings(null)"
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
  </div>
</template>

<script lang="ts" setup>
import {useNotificationCenter} from '@stekoe/vue-toast-notificationcenter';
import {computed, nextTick, onBeforeMount, onBeforeUnmount, onBeforeUpdate, ref, Ref, watch} from 'vue';
import {useI18n} from 'vue-i18n';
import {RouteLocationNamedRaw, useRoute, useRouter} from 'vue-router';

import {useApplicationStore} from '@/composables/useApplicationStore';
import Application from '@/services/application';
import NotificationFilter from '@/services/notification-filter';
import {anyValueMatches} from '@/utils/collections';
import {concatMap, mergeWith, Subject, timer} from '@/utils/rxjs';
import {groupApplicationsBy, GroupingType} from "@/services/instanceGroupService";
import ApplicationStats from "@/views/applications/ApplicationStats.vue";
import ApplicationNotificationCenter from "@/views/applications/ApplicationNotificationCenter.vue";
import ApplicationStatusHero from "@/views/applications/ApplicationStatusHero.vue";
import NotificationFilterSettings from "@/views/applications/NotificationFilterSettings.vue";
import ApplicationListItemAction from "@/views/applications/ApplicationListItemAction.vue";
import InstancesList from "@/views/applications/InstancesList.vue";
import Instance from "@/services/instance";
import Popper from "@/directives/popper";

const vPopper = Popper;

type NotificationFilterSettingsObject = { id: string; name: string };


const {t} = useI18n();
const router = useRouter();
const route = useRoute();
const {applications, applicationsInitialized, applicationStore} = useApplicationStore();
const notificationCenter = useNotificationCenter({});
const filter = ref(route.query.q?.toString());
const expandedGroups = ref([]);
const groupingCriterion = ref<GroupingType>('application');
const notificationFilterSubject = new Subject();
const notificationFilters = ref([]);
const hasNotificationFiltersSupport = ref(NotificationFilter.isSupported());
const showNotificationFilterSettingsObject = ref(
    null as unknown as NotificationFilterSettingsObject
);
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
  const predicate = (value: string) => String(value).toLowerCase().includes(term);
  return (
      anyValueMatches(instance.registration, predicate) ||
      anyValueMatches(instance.buildVersion, predicate) ||
      anyValueMatches(instance.id, predicate) ||
      anyValueMatches(instance.tags, predicate)
  );
};

const groupNames = computed(() => {
  return [...new Set(applications.value.flatMap(application => application.instances)
      .map(instance => instance.registration.metadata?.['group'] ?? "Ungrouped"))];
});

const grouped = computed(() => {
  const filteredApplications = filterInstances(applications);
  return groupApplicationsBy(filteredApplications, groupingCriterion.value);
})

const isGroupedByApplication = computed(() => {
  return groupingCriterion.value === 'application';
})

watch(applicationsInitialized, (initialized) => {
  if (initialized) {
    scrollIntoView(props.selected);
  }
})

watch(filter, (q) => {
  let to = {
    name: 'applications',
    params: {selected: props.selected},
  } as RouteLocationNamedRaw;

  if (q && q.length > 0) {
    to = {
      ...to,
      query: {
        q
      },
    } as RouteLocationNamedRaw;
  }

  router.replace(to);
});

watch(groupNames, (newValue) => {
  if (newValue.length === 1) {
    groupingCriterion.value = 'application';
  }
});

const hasActiveFilter = computed(() => {
  return filter.value && filter.value.length > 0;
});

const titleClick = (groupName: string) => {
  toggleGroup(groupName);
  select(groupName);
}

const select = (name: string) => {
  router.replace({
    name: 'applications',
    params: {selected: name},
  });
};

async function scrollIntoView(id: string) {
  if (id) {
    await nextTick();
    const el = document.getElementById(id);
    if (el) {
      const top = el.getBoundingClientRect().top + window.scrollY - 100;
      window.scroll({
        top,
        left: window.scrollX,
        behavior: 'smooth',
      });
    }
  }
}

const subscription = timer(0, 60000)
    .pipe(
        mergeWith(notificationFilterSubject),
        concatMap(fetchNotificationFilters)
    )
    .subscribe({
      next: (data) => {
        notificationFilters.value = data;
      },
      error: (error) => {
        console.warn(
            'Fetching notification filters failed with error:',
            error
        );
        notificationCenter.error(
            t('applications.fetching_notification_filters_failed')
        );
      },
    });

onBeforeUnmount(() => {
  subscription.unsubscribe();
});

async function fetchNotificationFilters() {
  if (hasNotificationFiltersSupport.value) {
    const response = await NotificationFilter.getFilters();
    return response.data;
  }
  return [];
}

async function addFilter({object, ttl}: { object: Application | Instance; ttl: number }) {
  try {
    const response = await NotificationFilter.addFilter(object, ttl);
    let notificationFilter = response.data;
    notificationFilterSubject.next(notificationFilter);
    notificationCenter.success(
        `${t('applications.notifications_suppressed_for', {
          name:
              notificationFilter.applicationName ||
              notificationFilter.instanceId,
        })} <strong>${notificationFilter.expiry.fromNow(true)}</strong>.`
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
    notificationCenter.success(
        t('applications.notification_filter.removed')
    );
  } catch (error) {
    console.warn('Deleting notification filter failed:', error);
  } finally {
    toggleNotificationFilterSettings(null);
  }
}

function toggleNotificationFilterSettings(obj) {
  showNotificationFilterSettingsObject.value = obj ? obj : null;
}

function filterInstances(applications: Ref<Application[]>) {
  if (!filter.value) {
    return applications.value;
  }

  return applications.value
      .map((application) =>
          application.filterInstances((i) =>
              instanceMatchesFilter(filter.value.toLowerCase(), i)
          )
      )
      .filter((application) => application.instances.length > 0);
}


const isExpanded = (name: string) => {
  return expandedGroups.value.includes(name);
}

const toggleGroup = (name: string) => {
  if (isExpanded(name)) {
    expandedGroups.value = expandedGroups.value.filter((n) => n !== name);
  } else {
    expandedGroups.value = [...expandedGroups.value, name];
  }
}
</script>

<script lang="ts">
import handle from "./handle.vue";

export default {
  install({viewRegistry}) {
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
};
</script>
