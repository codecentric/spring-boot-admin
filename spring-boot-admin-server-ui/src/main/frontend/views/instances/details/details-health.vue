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
    :id="`health-details-panel__${instanceId}`"
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
        :to="{ name: 'journal', query: { instanceId: instanceId } }"
        class="text-sm inline-flex items-center leading-sm border border-gray-400 bg-white text-gray-700 rounded overflow-hidden px-3 py-1 hover:bg-gray-200 ml-1"
      >
        <font-awesome-icon :icon="faScroll" />
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
          :instance-id="instanceId"
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
                  :icon="faChevronRight"
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
              :instance-id="instanceId"
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

<script setup lang="ts">
import { faChevronRight, faScroll } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { computed, ref, watch } from 'vue';

import SbaAccordion from '@/components/sba-accordion.vue';

import { useInstanceData } from '@/composables/useInstanceData';
import { useInstanceService } from '@/composables/useInstanceService';
import HealthDetails from '@/views/instances/details/health-details.vue';

const props = defineProps<{
  instanceId: string;
}>();

const { instance } = useInstanceData(() => props.instanceId);
const { fetchHealth, fetchHealthGroup } = useInstanceService(props.instanceId);

const panelOpen = ref(true);

const health = computed(
  () => instance.value?.statusInfo ?? { status: 'UNKNOWN', details: {} },
);
const hasHealthUrl = computed(
  () =>
    instance.value?.endpoints?.some((e: { id: string }) => e.id === 'health') ??
    false,
);

interface HealthGroup {
  name: string;
  data: Record<string, any> | null;
}

const healthGroups = ref<HealthGroup[]>([]);
const healthGroupsError = ref<Error | null>(null);
const healthGroupOpenStatus = ref<
  Record<string, { isOpen: boolean; collapsible: boolean }>
>({});
const healthGroupLoadingMap = ref<Record<string, boolean>>({});

const isHealthGroupOpen = (groupName: string) =>
  healthGroupOpenStatus.value[groupName]?.isOpen ?? false;

const isHealthGroupCollapsible = (groupName: string) =>
  healthGroupOpenStatus.value[groupName]?.collapsible ?? true;

async function fetchHealthGroups() {
  if (!hasHealthUrl.value) return;
  healthGroupsError.value = null;
  try {
    const res = await fetchHealth();
    if (Array.isArray(res.data.groups)) {
      healthGroups.value = res.data.groups.map((name: string) => ({
        name,
        data: null,
      }));
      healthGroupOpenStatus.value = {};
      healthGroupLoadingMap.value = {};
    } else {
      healthGroups.value = [];
    }
  } catch (error) {
    console.warn('Fetching health groups failed:', error);
    healthGroupsError.value = error as Error;
  }
}

async function fetchGroupDetails(groupName: string) {
  healthGroupLoadingMap.value[groupName] = true;
  try {
    const res = await fetchHealthGroup(groupName);
    const group = healthGroups.value.find((g) => g.name === groupName);
    if (group) {
      group.data = res.data;
      healthGroupOpenStatus.value[groupName] = {
        isOpen: true,
        collapsible: res.data !== undefined,
      };
    }
  } catch (error) {
    console.warn(`Fetching health group '${groupName}' failed:`, error);
  } finally {
    healthGroupLoadingMap.value[groupName] = false;
  }
}

function toggleHealthGroup(groupName: string) {
  const group = healthGroups.value.find((g) => g.name === groupName);
  if (group == undefined) return;
  if (group.data === null) {
    fetchGroupDetails(groupName);
  } else if (isHealthGroupCollapsible(groupName)) {
    healthGroupOpenStatus.value[groupName].isOpen =
      !healthGroupOpenStatus.value[groupName].isOpen;
  }
}

watch(
  () => props.instanceId,
  (newId, oldId) => {
    if (newId === oldId) {
      for (const group of healthGroups.value) {
        group.data = null;
      }
      healthGroupOpenStatus.value = {};
      healthGroupLoadingMap.value = {};
    } else {
      healthGroups.value = [];
      healthGroupOpenStatus.value = {};
      healthGroupLoadingMap.value = {};
      healthGroupsError.value = null;
    }
    fetchHealthGroups();
  },
  { immediate: true },
);
</script>
