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

      <template v-if="!healthGroupsError">
        <div class="health-panel">
          <!-- ── Component list ─────────────────────────────────────────── -->
          <div v-if="hasComponents" class="health-section">
            <div class="health-section__body">
              <health-details
                v-for="([compName, compData], idx) in componentEntries"
                :key="`${compName}_${idx}`"
                :instance="instance"
                :name="compName"
                :health="compData"
              />
            </div>
          </div>

          <!-- ── Health groups ──────────────────────────────────────────── -->
          <template v-for="group in healthGroups" :key="group.name">
            <div class="health-group">
              <button
                class="health-group__header"
                :aria-label="
                  $t('instances.details.health_group.title') + ': ' + group.name
                "
                @click="toggleHealthGroup(group.name)"
              >
                <span class="health-group__name">{{ group.name }}</span>
                <div class="health-group__body-col">
                  <sba-status-badge
                    v-if="group.data?.status"
                    :status="group.data.status"
                  />
                  <font-awesome-icon
                    v-if="healthGroupLoadingMap[group.name]"
                    icon="sync-alt"
                    spin
                    class="h-3 text-gray-400"
                  />
                </div>
                <font-awesome-icon
                  v-if="isHealthGroupCollapsible(group.name)"
                  :icon="faChevronRight()"
                  class="health-group__chevron transition-transform"
                  :class="{ 'rotate-90': isHealthGroupOpen(group.name) }"
                />
              </button>

              <div
                v-if="isHealthGroupOpen(group.name) && group.data"
                class="health-group__body"
              >
                <health-details
                  :instance="instance"
                  :health="group.data"
                  :name="group.name"
                />
              </div>
            </div>
          </template>
        </div>
      </template>
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
    overallStatusClass(): string {
      return (this.health?.status ?? 'unknown')
        .toLowerCase()
        .replace(/_/g, '-');
    },
    hasComponents(): boolean {
      const source = this.health?.details ?? this.health?.components;
      return (
        source != null &&
        typeof source === 'object' &&
        Object.keys(source).length > 0
      );
    },
    componentEntries(): [string, any][] {
      const source = this.health?.details ?? this.health?.components;
      if (source && typeof source === 'object') {
        return Object.entries(source);
      }
      return [];
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
      if (group == undefined) return;

      if (group.data === null) {
        this.fetchGroupDetails(groupName);
      } else if (this.isHealthGroupCollapsible(groupName)) {
        this.healthGroupOpenStatus[groupName].isOpen =
          !this.healthGroupOpenStatus[groupName].isOpen;
      }
    },
    async fetchHealthGroups() {
      if (!this.hasHealthUrl) return;

      this.healthGroupsError = null;

      try {
        const res = await this.instance.fetchCachedHealthGroups();

        if (Array.isArray(res.data)) {
          const currentNames = this.healthGroups.map((g) => g.name);
          const incomingNames = res.data;
          const unchanged =
            currentNames.length === incomingNames.length &&
            currentNames.every((name, idx) => name === incomingNames[idx]);

          if (unchanged) return;

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

<style scoped>
@reference "../../../index.css";

/* ── Status colour tokens — sourced from theme.css @theme ──────────────── */
.status--up {
  --status-color: var(--color-status-up);
}
.status--down,
.status--offline,
.status--out-of-service {
  --status-color: var(--color-status-down);
}
.status--restricted {
  --status-color: var(--color-status-restricted);
}
.status--unknown {
  --status-color: var(--color-status-unknown);
}

/* ── Outer panel wrapper ───────────────────────────────────────────────── */
.health-panel {
  @apply -mx-4 -my-3 divide-y divide-gray-100;
}

/* ── Overview row (overall status) ────────────────────────────────────── */
.health-overview {
  @apply flex items-stretch bg-gray-50;
}

.health-overview__bar {
  @apply w-1 shrink-0;
  background-color: var(--status-color, var(--color-status-unknown));
}

.health-overview__label {
  @apply w-52 shrink-0 px-4 py-3 text-sm font-semibold text-gray-700 self-center;
}

.health-overview__body {
  @apply flex-1 py-3 min-w-0 self-center;
}

/* ── Component section ─────────────────────────────────────────────────── */
.health-section__body {
  @apply divide-y divide-gray-100;
}

/* ── Health group ──────────────────────────────────────────────────────── */
.health-group {
  @apply bg-white border-b border-gray-100;
}

.health-group__header {
  @apply w-full flex items-center text-left
         hover:bg-gray-50 transition-colors cursor-pointer
         border-none bg-transparent p-0;
}

.health-group__name {
  @apply w-52 shrink-0 px-4 py-3 text-xs font-semibold text-gray-500
         tracking-wide break-all self-center;
}

.health-group__body-col {
  @apply flex-1 py-3 min-w-0 flex items-center gap-2;
}

.health-group__chevron {
  @apply text-gray-400 text-xs shrink-0 px-4 self-center;
}

.health-group__body {
  @apply border-t border-gray-100;
}
</style>
