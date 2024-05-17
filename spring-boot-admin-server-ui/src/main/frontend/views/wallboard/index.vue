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
      class="flex gap-2 justify-end absolute w-full md:w-[28rem] top-14 right-0 bg-black/20 py-3 px-4 rounded-bl"
    >
      <sba-input
        v-model="routerState.termFilter"
        class="flex-1"
        :placeholder="$t('term.filter')"
        name="filter"
        type="search"
      >
        <template #prepend>
          <font-awesome-icon icon="filter" />
        </template>
      </sba-input>

      <select
        v-if="healthStatus.size > 1"
        v-model="routerState.statusFilter"
        aria-label="status-filter"
        class="relative focus:z-10 focus:ring-indigo-500 focus:border-indigo-500 block sm:text-sm border-gray-300 rounded"
      >
        <option selected value="none" v-text="$t('term.all')" />
        <optgroup :label="t('health.label')">
          <option
            v-for="status in healthStatus"
            :key="status"
            :value="status"
            v-text="t('health.status.' + status)"
          />
        </optgroup>
      </select>
    </div>

    <sba-alert
      v-if="error"
      :error="error"
      :title="t('applications.server_connection_failed')"
      class="my-0 fixed w-full"
      severity="WARN"
    />

    <sba-loading-spinner v-if="!applicationsInitialized" />

    <template v-if="applicationsInitialized">
      <div
        v-if="routerState.termFilter.length > 0 && applications.length === 0"
        class="flex w-full h-full items-center text-center text-white text-xl"
        v-text="
          t('term.no_results_for_term', {
            term: routerState.termFilter,
          })
        "
      />
      <hex-mesh
        v-if="applicationsInitialized"
        :class-for-item="classForApplication"
        :items="applications"
        @click="select"
      >
        <template #item="{ item: application }">
          <div :key="application.name" class="hex__body application">
            <div class="application__status-indicator" />
            <div class="application__header application__time-ago is-muted">
              <sba-time-ago :date="application.statusTimestamp" />
            </div>
            <div class="application__body">
              <h1 class="application__name" v-text="application.name" />
              <p
                class="application__instances is-muted"
                v-text="
                  t('wallboard.instances_count', application.instances.length)
                "
              />
            </div>
            <h2
              class="application__footer application__version"
              v-text="application.buildVersion"
            />
          </div>
        </template>
      </hex-mesh>
    </template>
  </section>
</template>

<script lang="ts">
import classNames from 'classnames';
import Fuse from 'fuse.js';
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import { HealthStatus } from '@/HealthStatus';
import { useApplicationStore } from '@/composables/useApplicationStore';
import Application from '@/services/application';
import { useRouterState } from '@/utils/useRouterState';
import hexMesh from '@/views/wallboard/hex-mesh.vue';

export default {
  components: { hexMesh },
  setup() {
    const { t } = useI18n();

    const routerState = useRouterState({
      termFilter: '',
      wordWrap: true,
      statusFilter: 'none',
    });

    const { applications, applicationsInitialized, error } =
      useApplicationStore();

    const fuse = computed(
      () =>
        new Fuse<Application>(applications.value, {
          includeScore: true,
          useExtendedSearch: true,
          threshold: 0.4,
          keys: ['name', 'buildVersion', 'instances.name', 'instances.id'],
        }),
    );

    const filteredApplications = computed(() => {
      function filterByTerm(): Application[] {
        if (routerState.termFilter.length > 0) {
          return fuse.value.search(routerState.termFilter).map((sr) => sr.item);
        } else {
          return applications.value;
        }
      }

      function filterByStatus(result: Application[]) {
        if (routerState.statusFilter !== 'none') {
          return result.filter(
            (application: Application) =>
              application.status === routerState.statusFilter,
          );
        }

        return result;
      }

      let result = filterByTerm();
      result = filterByStatus(result);

      return result;
    });

    const healthStatus = computed(() => {
      return new Set(
        applications.value.map((application) => application.status),
      );
    });

    return {
      applications: filteredApplications,
      applicationsInitialized,
      error,
      t,
      healthStatus,
      routerState,
    };
  },
  methods: {
    classNames,
    classForApplication(application: Application) {
      if (!application) {
        return null;
      }
      if (application.status === HealthStatus.UP) {
        return 'up';
      }
      if (application.status === HealthStatus.RESTRICTED) {
        return 'restricted';
      }
      if (application.status === HealthStatus.DOWN) {
        return 'down';
      }
      if (application.status === HealthStatus.OUT_OF_SERVICE) {
        return 'down';
      }
      if (application.status === HealthStatus.OFFLINE) {
        return 'down';
      }
      if (application.status === HealthStatus.UNKNOWN) {
        return 'unknown';
      }
      return '';
    },
    select(application: Application) {
      if (application.instances.length === 1) {
        this.$router.push({
          name: 'instances/details',
          params: { instanceId: application.instances[0].id },
        });
      } else {
        this.$router.push({
          name: 'applications',
          params: { selected: application.name },
        });
      }
    },
  },
  install({ viewRegistry }) {
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

.restricted > polygon {
  stroke: theme('colors.yellow.400');
  fill: theme('colors.yellow.400');
}
</style>
