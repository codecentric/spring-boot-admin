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
  <sba-panel :title="$t('instances.details.health.title')" :loading="loading">
    <template #actions>
      <router-link
        :to="{ name: 'journal', query: { instanceId: instance.id } }"
        class="text-sm inline-flex items-center leading-sm border border-gray-400 bg-white text-gray-700 rounded overflow-hidden px-3 py-1 hover:bg-gray-200 ml-1"
      >
        <font-awesome-icon icon="history" />
      </router-link>
    </template>

    <template #default>
      <sba-alert
        :error="error"
        class="border-l-4"
        :title="$t('term.fetch_failed')"
      />
      <div class="-mx-4 -my-3">
        <health-details :health="health" name="Instance" />

        <template v-for="healthGroup in healthGroups" :key="healthGroup.name">
          <div
            class="px-4 py-2 border-t sm:px-6"
            :class="{ 'border-b': isHealthGroupOpen(healthGroup.name) }"
          >
            <h4 class="leading-6 font-medium text-gray-900">
              <button
                class="flex items-center"
                :aria-label="
                  $t('instances.details.health_group.title') +
                  ': ' +
                  healthGroup.name
                "
                @click="toggleHealthGroup(healthGroup.name)"
              >
                <font-awesome-icon
                  v-if="isHealthGroupCollapsible(healthGroup.name)"
                  icon="chevron-down"
                  class="transition-[transform] mr-2 h-4"
                  :class="{
                    '-rotate-90': !isHealthGroupOpen(healthGroup.name),
                  }"
                />
                <span v-text="$t('instances.details.health_group.title')"></span
                >:&nbsp;
                <span v-text="healthGroup.name"></span>
              </button>
            </h4>
          </div>
          <div v-if="isHealthGroupOpen(healthGroup.name)">
            <health-details
              :health="healthGroup.data"
              :name="healthGroup.name"
            />
          </div>
        </template>
      </div>
    </template>
  </sba-panel>
</template>

<script lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

import Instance from '@/services/instance';
import healthDetails from '@/views/instances/details/health-details';

export default {
  components: { FontAwesomeIcon, healthDetails },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    error: null,
    loading: false,
    liveHealth: null,
    healthGroups: [],
    healthGroupOpenStatus: {} as {
      isOpen: boolean;
      collapsible: boolean;
    },
  }),
  computed: {
    health() {
      return this.liveHealth || this.instance.statusInfo;
    },
  },
  created() {
    this.fetchHealth();
  },
  methods: {
    isHealthGroupOpen(groupName: string) {
      return this.healthGroupOpenStatus[groupName].isOpen === true;
    },
    isHealthGroupCollapsible(groupName: string) {
      return this.healthGroupOpenStatus[groupName].collapsible;
    },
    toggleHealthGroup(groupName: string) {
      if (this.isHealthGroupCollapsible(groupName)) {
        this.healthGroupOpenStatus[groupName].isOpen =
          !this.healthGroupOpenStatus[groupName].isOpen;
      }
    },
    async fetchHealth() {
      this.error = null;
      this.loading = true;
      try {
        const res = await this.instance.fetchHealth();
        this.liveHealth = res.data;

        if (Array.isArray(res.data.groups)) {
          this.healthGroups = (
            await Promise.allSettled(
              res.data.groups.map(async (group: string) => {
                return {
                  name: group,
                  data: (await this.instance.fetchHealthGroup(group)).data,
                };
              }),
            )
          ).map((group) =>
            group.status === 'fulfilled' ? group.value : group.reason,
          );

          this.healthGroupOpenStatus = this.healthGroups
            .map(
              (group: {
                name: string;
                data: { details: string } | undefined;
              }) => {
                return {
                  [group.name]: {
                    isOpen: group.data?.details === undefined,
                    collapsible: group.data?.details !== undefined,
                  },
                };
              },
            )
            .reduce((acc, curr) => ({ ...acc, ...curr }), {});
        }
      } catch (error) {
        console.warn('Fetching live health failed:', error);
        this.error = error;
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>
