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
  <sba-instance-section :error="error">
    <template #before>
      <sba-sticky-subnav>
        <div class="flex gap-2">
          <sba-action-button-scoped
            :action-fn="clearCaches"
            :instance-count="application.instances.length"
            :show-info="false"
            @scope-changed="changeScope"
          >
            <template #default="slotProps">
              <span
                v-if="slotProps.refreshStatus === 'completed'"
                v-text="$t('term.execution_successful')"
              />
              <span
                v-else-if="slotProps.refreshStatus === 'failed'"
                v-text="$t('term.execution_failed')"
              />
              <span v-else>
                <font-awesome-icon icon="trash" />&nbsp;
                <span v-text="$t('term.clear')" />
              </span>
            </template>
          </sba-action-button-scoped>

          <div class="flex-1">
            <sba-input
              v-model="filter"
              :placeholder="$t('term.filter')"
              name="filter"
              type="search"
            >
              <template #prepend>
                <font-awesome-icon icon="filter" />
              </template>
              <template #append>
                <span class="button is-static">
                  <span v-text="filteredCaches.length" />
                  /
                  <span v-text="caches.length" />
                </span>
              </template>
            </sba-input>
          </div>
        </div>
      </sba-sticky-subnav>
    </template>

    <sba-panel>
      <caches-list
        :application="application"
        :caches="filteredCaches"
        :instance="instance"
        :scope="scope"
        :is-loading="isLoading"
      />
    </sba-panel>
  </sba-instance-section>
</template>

<script>
import { flatMap, isEmpty } from 'lodash-es';

import { ActionScope } from '@/components/ActionScope';

import Application from '@/services/application';
import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import CachesList from '@/views/instances/caches/caches-list';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

const flattenCaches = (cacheData) => {
  if (isEmpty(cacheData.cacheManagers)) {
    return [];
  }
  const mappend = flatMap(
    Object.entries(cacheData.cacheManagers),
    ([cacheManagerName, v]) =>
      Object.keys(v.caches).map((cacheName) => ({
        cacheManager: cacheManagerName,
        name: cacheName,
        key: `${cacheManagerName}:${cacheName}`,
      })),
  );
  return mappend.sort((c1, c2) => c1.key.localeCompare(c2.key));
};

export default {
  components: { SbaInstanceSection, CachesList },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
    application: {
      type: Application,
      required: true,
    },
  },
  data: () => ({
    isLoading: false,
    error: null,
    caches: [],
    filter: '',
    scope: ActionScope.INSTANCE,
  }),
  computed: {
    filteredCaches() {
      let filterFn = this.getFilterFn();
      return filterFn ? this.caches.filter(filterFn) : this.caches;
    },
  },
  created() {
    this.fetchCaches();
  },
  methods: {
    clearCaches(scope) {
      if (scope === 'instance') {
        return this.instance.clearCaches();
      } else {
        return this.application.clearCaches();
      }
    },
    async fetchCaches() {
      this.error = null;
      this.isLoading = true;
      try {
        const res = await this.instance.fetchCaches();
        this.caches = flattenCaches(res.data);
      } catch (error) {
        console.warn('Fetching caches failed:', error);
        this.error = error;
      }
      this.isLoading = false;
    },
    getFilterFn() {
      let filterFn = null;

      if (this.filter) {
        const normalizedFilter = this.filter.toLowerCase();
        filterFn = (cache) =>
          cache.name.toLowerCase().includes(normalizedFilter);
      }

      return filterFn;
    },
    changeScope(scope) {
      this.scope = scope;
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/caches',
      parent: 'instances',
      path: 'caches',
      label: 'instances.caches.label',
      group: VIEW_GROUP.DATA,
      component: this,
      order: 970,
      isEnabled: ({ instance }) => instance.hasEndpoint('caches'),
    });
  },
};
</script>
