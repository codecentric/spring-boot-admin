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
    <div
        class="flex gap-2 justify-end absolute w-full md:w-[28rem] items-stretch top-14 right-0 bg-black/20 py-3 px-4 rounded-bl"
    >
      <sba-input
          v-model="termFilter"
          class="flex-1"
          :placeholder="$t('term.filter')"
          name="filter"
          type="search"
      >
        <template #prepend>
          <font-awesome-icon icon="filter"/>
        </template>
      </sba-input>

      <select
          aria-label="status-filter"
          v-if="healthStatus.size > 1"
          v-model="statusFilter"
          class="relative focus:z-10 focus:ring-indigo-500 focus:border-indigo-500 block sm:text-sm border-gray-300 rounded"
      >
        <option selected value="none" v-text="$t('term.all')"/>
        <optgroup :label="t('health.label')">
          <option
              v-for="status in healthStatus"
              :key="status"
              :value="status"
              v-text="t('health.status.' + status)"
          />
        </optgroup>
      </select>

      <div class="text-right rounded h-full" v-if="groupNames.length > 1">
        <sba-button-group>
          <sba-button size="base"
                      @click="() => groupingCriterion = 'application'"
                      :title="t('term.group_by.application')">
            <font-awesome-icon icon="list"/>
          </sba-button>
          <sba-button size="base"
                      @click="() => groupingCriterion = 'group'"
                      :title="t('term.group_by.group')">
            <font-awesome-icon icon="expand"/>
          </sba-button>
        </sba-button-group>
      </div>
    </div>

    <sba-alert
        v-if="error"
        :error="error"
        :title="t('applications.server_connection_failed')"
        class="my-0 fixed w-full"
        severity="WARN"
    />

    <sba-loading-spinner v-if="!applicationsInitialized"/>

    <template v-if="applicationsInitialized">
      <div
          v-if="termFilter.length > 0 && applications.length === 0"
          class="flex w-full h-full items-center text-center text-white text-xl"
          v-text="
          t('term.no_results_for_term', {
            term: termFilter,
          })
        "
      />
      <hex-mesh
          v-if="applicationsInitialized"
          :class-for-item="classForApplication"
          :items="applications"
          @click="select"
      >
        <template #item="{ item }">
          <div :key="item.name" class="hex__body application">
            <div class="application__status-indicator"/>
            <div class="application__header application__time-ago is-muted" v-if="groupingCriterion === 'application'">
              <sba-time-ago :date="item.statusTimestamp"/>
            </div>
            <div class="application__body">
              <h1 class="application__name" v-text="t(item.name)"/>
              <p
                  class="application__instances is-muted"
                  v-text="
                  t('wallboard.instances_count', item.instances.length)
                "
              />
            </div>
            <h2
                class="application__footer application__version"
                v-text="item.buildVersion"
            />
          </div>
        </template>
      </hex-mesh>
    </template>
  </section>
</template>

<script lang="ts">
import Fuse from 'fuse.js';
import {computed, ref, watch} from 'vue';
import {useI18n} from 'vue-i18n';

import {HealthStatus} from '@/HealthStatus';
import {useApplicationStore} from '@/composables/useApplicationStore';
import hexMesh from '@/views/wallboard/hex-mesh';
import {groupApplicationsBy, GroupingType, InstancesListItem, isGroupingType} from "@/services/instanceGroupService";
import {RouteLocationNamedRaw, useRoute, useRouter} from "vue-router";

export default {
  components: {hexMesh},
  setup() {
    const {t} = useI18n();
    const route = useRoute();
    const router = useRouter();
    const {applications, applicationsInitialized, error} = useApplicationStore();

    const termFilter = ref('');
    const statusFilter = ref(route.query?.status?.toString() ?? 'none');


    const queryParamGroupBy = route.query?.groupBy?.toString();
    const groupingCriterion = ref<GroupingType>(isGroupingType(queryParamGroupBy) ? queryParamGroupBy as GroupingType : 'application');
    const groupNames = computed(() => {
      return [...new Set(applications.value.flatMap(application => application.instances)
          .map(instance => instance.registration?.metadata?.['group'] ?? "Ungrouped"))];
    });

    watch(groupingCriterion, (groupBy) => {
      if (groupBy && groupBy.length > 0) {
        const to = {
          name: 'wallboard',
          query: {
            ...route.query,
            groupBy
          },
        } as RouteLocationNamedRaw;
        router.replace(to);
      }
    });

    watch(statusFilter, (_statusFilter) => {
      if (_statusFilter && _statusFilter.length > 0) {
        const to = {
          name: 'wallboard',
          query: {
            ...route.query,
            status: _statusFilter
          },
        } as RouteLocationNamedRaw;
        router.replace(to);
      }
    });

    const fuse = computed(
        () =>
            new Fuse(applications.value, {
              includeScore: true,
              useExtendedSearch: true,
              threshold: 0.25,
              keys: ['name', 'buildVersion', 'instances.name', 'instances.id'],
            }),
    );

    const groupedApplications = computed(() => {
      function filterByTerm() {
        if (termFilter.value.length > 0) {
          return fuse.value.search(termFilter.value).map((sr) => sr.item);
        } else {
          return applications.value;
        }
      }

      function filterByStatus(result) {
        if (statusFilter.value !== 'none') {
          return result.filter(
              (application) => application.status === statusFilter.value,
          );
        }

        return result;
      }

      let result = filterByTerm();
      result = filterByStatus(result);

      return groupApplicationsBy(result, groupingCriterion.value);
    });

    const healthStatus = computed(() => {
      return new Set(applications.value.map((application) => application.status));
    });

    return {
      applications: groupedApplications,
      groupingCriterion,
      groupNames,
      applicationsInitialized,
      error,
      t,
      termFilter,
      statusFilter,
      healthStatus,
    };
  },
  methods: {
    classForApplication(instancesListItem: InstancesListItem) {
      if (!instancesListItem) {
        return null;
      }

      if (instancesListItem.status === HealthStatus.UP) {
        return 'up';
      }
      if (instancesListItem.status === HealthStatus.RESTRICTED) {
        return 'restricted';
      }
      if (instancesListItem.status === HealthStatus.DOWN) {
        return 'down';
      }
      if (instancesListItem.status === HealthStatus.OUT_OF_SERVICE) {
        return 'down';
      }
      if (instancesListItem.status === HealthStatus.OFFLINE) {
        return 'down';
      }
      if (instancesListItem.status === HealthStatus.UNKNOWN) {
        return 'unknown';
      }
      return 'unknown';
    },
    select(instancesListItem: InstancesListItem) {
      if (instancesListItem.instances.length === 1) {
        return this.$router.push({
          name: 'instances/details',
          params: {instanceId: instancesListItem.instances[0].id},
        });
      } else {
        this.$router.push({
          name: 'applications',
          params: {selected: instancesListItem.name},
          query: {
            groupBy: this.groupingCriterion
          }
        });
      }
    },
  },
  install({viewRegistry}) {
    viewRegistry.addView({
      path: '/wallboard',
      name: 'wallboard',
      label: 'wallboard.label',
      order: -100,
      component: this,
    });
  },
};
</script>

<style lang="postcss">
.wallboard {
  background-color: #4a4a4a;
  height: calc(100vh - 52px);
  width: 100%;
}

.wallboard .application {
  color: #f5f5f5;
  font-size: 1em;
  font-weight: 400;
  line-height: 1;
  text-align: center;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.wallboard .application__name {
  width: 100%;
  padding: 2.5%;
  color: #fff;
  word-break: break-word;
  font-size: 2em;
  font-weight: 600;
  line-height: 1.125;
}

.wallboard .application__version {
  color: #f5f5f5;
  font-size: 1.25em;
  line-height: 1.25;
}

.wallboard .application__header {
  width: 90%;
  margin-bottom: 0.5em;
}

.wallboard .application__footer {
  width: 90%;
  margin-top: 0.5em;
}

.up > polygon {
  stroke: theme('colors.green.400');
  fill: theme('colors.green.400');
}

.down > polygon,
.offline > polygon {
  stroke: theme('colors.red.400');
  fill: theme('colors.red.400');
  stroke-width: 2;
}

.unknown > polygon {
  stroke: theme('colors.gray.400');
  fill: theme('colors.gray.400');
}

.restricted > polygon {
  stroke: theme('colors.yellow.400');
  fill: theme('colors.yellow.400');
}

.hex .hex__body::after {
  display: flex;
  justify-content: center;
  align-content: center;
  font-size: 15em;
  position: absolute;
  z-index: -1;
  width: 100%;
}

.hex .hex__body {
  position: fixed;
  z-index: 10;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.hex.down .hex__body::after {
  content: '!';
  color: theme('colors.red.400');
}

.hex.unknown .hex__body::after {
  content: '?';
  color: theme('colors.gray.500');
}
</style>
