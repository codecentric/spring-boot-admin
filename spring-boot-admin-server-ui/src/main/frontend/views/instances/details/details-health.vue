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
  <sba-accordion
    :id="`health-details-panel__${instance.id}`"
    v-model="panelOpen"
    :title="$t('instances.details.health.title')"
  >
    <template #title>
      <sba-status-badge
        v-if="health.status"
        :status="health.status"
        class="ml-2 transition-opacity"
        :class="{ 'opacity-0': panelOpen }"
      />
    </template>

    <template #actions>
      <router-link
        v-if="hasHealthUrl"
        :title="$t('applications.actions.journal')"
        :to="{ name: 'journal', query: { instanceId: instance.id } }"
        class="text-sm inline-flex items-center leading-sm border border-gray-400 bg-white text-gray-700 rounded overflow-hidden px-3 py-1 hover:bg-gray-200 ml-1"
      >
        <font-awesome-icon :icon="faScroll()" />
      </router-link>
    </template>

    <template #default>
      <sba-alert
        v-if="healthGroupsError"
        :error="healthGroupsError"
        class="border-l-4"
        :title="$t('term.fetch_failed')"
      />
      <div class="-mx-4 -my-3">
        <health-details
          :instance="instance"
          :health="health"
          :index="0"
          name="Instance"
        />

        <template v-for="(group, groupIdx) in healthGroups" :key="group.name">
          <div
            class="px-4 py-2 border-t border-gray-200 sm:px-6"
            :class="{ 'border-b': isHealthGroupOpen(group.name) }"
          >
            <h4 class="leading-6 font-medium text-gray-900">
              <button
                class="flex items-center"
                :aria-label="
                  $t('instances.details.health_group.title') + ': ' + group.name
                "
                @click="toggleHealthGroup(group.name)"
              >
                <font-awesome-icon
                  v-if="isHealthGroupCollapsible(group.name)"
                  :icon="faChevronRight()"
                  class="transition-transform mr-2 h-4"
                  :class="{
                    'rotate-90': isHealthGroupOpen(group.name),
                  }"
                />
                <span
                  v-text="$t('instances.details.health_group.title')"
                />:&nbsp; <span v-text="group.name"></span>
                <sba-status-badge
                  v-if="group.data?.status"
                  class="ml-2 fade"
                  :status="group.data?.status"
                />

                <font-awesome-icon
                  v-if="healthGroupLoadingMap[group.name]"
                  icon="sync-alt"
                  spin
                  class="ml-2 h-3"
                />
              </button>
            </h4>
          </div>
          <div v-if="isHealthGroupOpen(group.name) && group.data">
            <health-details
              :instance="instance"
              :health="group.data"
              :name="group.name"
              :index="groupIdx + 1"
            />
          </div>
        </template>
      </div>
    </template>
  </sba-accordion>
</template>

<script lang="ts">
import { faChevronRight, faScroll } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { defineComponent } from 'vue';

import SbaAccordion from '@/components/sba-accordion.vue';

import Instance from '@/services/instance';
import HealthDetails from '@/views/instances/details/health-details.vue';

export default defineComponent({
  components: { SbaAccordion, FontAwesomeIcon, HealthDetails },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    panelOpen: true,
    healthGroups: [] as Array<{
      name: string;
      data: Record<string, any> | null;
    }>,
    healthGroupsError: null as Error | null,
    healthGroupOpenStatus: {} as Record<
      string,
      { isOpen: boolean; collapsible: boolean }
    >,
    healthGroupLoadingMap: {} as Record<string, boolean>,
    currentInstanceId: null as string | null,
  }),
  computed: {
    health() {
      return this.instance.statusInfo;
    },
    hasHealthUrl() {
      if (this.instance.endpoints) {
        return (
          this.instance.endpoints.findIndex(
            (endpoint: { id: string }) => endpoint.id === 'health',
          ) >= 0
        );
      }
      return false;
    },
    healthGroupsKey() {
      // Only re-fetch on meaningful status changes; deliberately excludes
      return `${this.instance.id}:${this.instance.statusInfo?.status ?? ''}:${this.instance.statusTimestamp ?? ''}`;
    },
  },
  watch: {
    healthGroupsKey: {
      handler: 'onInstanceChanged',
      immediate: true,
    },
  },
  methods: {
    faScroll() {
      return faScroll;
    },
    faChevronRight() {
      return faChevronRight;
    },
    onInstanceChanged() {
      if (this.instance.id !== this.currentInstanceId) {
        this.currentInstanceId = this.instance.id;
        this.healthGroups = [];
        this.healthGroupOpenStatus = {};
        this.healthGroupLoadingMap = {};
        this.healthGroupsError = null;
      }
      // Re-fetch the (server-cached) group list on every instance change
      this.fetchHealthGroups();
    },
    isHealthGroupOpen(groupName: string) {
      return this.healthGroupOpenStatus[groupName]?.isOpen ?? false;
    },
    isHealthGroupCollapsible(groupName: string) {
      return this.healthGroupOpenStatus[groupName]?.collapsible ?? true;
    },
    toggleHealthGroup(groupName: string) {
      const group = this.healthGroups.find((g) => g.name === groupName);

      if (group == undefined) {
        return;
      }

      if (group.data === null) {
        // First click — fetch group details
        this.fetchGroupDetails(groupName);
      } else if (this.isHealthGroupCollapsible(groupName)) {
        this.healthGroupOpenStatus[groupName].isOpen =
          !this.healthGroupOpenStatus[groupName].isOpen;
      }
    },
    async fetchHealthGroups() {
      if (!this.hasHealthUrl) {
        return;
      }

      this.healthGroupsError = null;

      try {
        const res = await this.instance.fetchCachedHealthGroups();

        if (Array.isArray(res.data)) {
          const currentNames = this.healthGroups.map((g) => g.name);
          const incomingNames = res.data;
          const unchanged =
            currentNames.length === incomingNames.length &&
            currentNames.every((name, idx) => name === incomingNames[idx]);

          // Skip reassignment when the group list is unchanged to preserve
          // the accordion open/loading state and avoid needless re-renders.
          if (unchanged) {
            return;
          }

          this.healthGroups = incomingNames.map((name: string) => ({
            name,
            data: null,
          }));
          this.healthGroupOpenStatus = {};
          this.healthGroupLoadingMap = {};
        } else {
          this.healthGroups = [];
        }
      } catch (error) {
        console.warn('Fetching health groups failed:', error);
        this.healthGroupsError = error as Error;
      }
    },
    async fetchGroupDetails(groupName: string) {
      this.healthGroupLoadingMap[groupName] = true;

      try {
        const res = await this.instance.fetchHealthGroup(groupName);
        const group = this.healthGroups.find((g) => g.name === groupName);

        if (group) {
          group.data = res.data;
          this.healthGroupOpenStatus[groupName] = {
            isOpen: true,
            collapsible: res.data !== undefined,
          };
        }
      } catch (error) {
        console.warn(`Fetching health group '${groupName}' failed:`, error);
      } finally {
        this.healthGroupLoadingMap[groupName] = false;
      }
    },
  },
});
</script>
