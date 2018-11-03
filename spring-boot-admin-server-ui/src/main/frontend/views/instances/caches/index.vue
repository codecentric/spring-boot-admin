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
  <section class="section">
    <div
      class="container"
    >
      <div
        v-if="error"
        class="message is-danger"
      >
        <div class="message-body">
          <strong>
            <font-awesome-icon
              class="has-text-danger"
              icon="exclamation-triangle"
            />
            Fetching caches failed.
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <div class="field-body">
        <div class="field has-addons">
          <p class="control is-expanded">
            <input
              class="input"
              type="search"
              placeholder="name filter"
              v-model="filter"
            >
          </p>
          <p class="control">
            <span class="button is-static">
              <span v-text="filteredCaches.length" />
              /
              <span v-text="caches.length" />
            </span>
          </p>
        </div>
      </div>
      <caches-list :instance="instance" :caches="filteredCaches" :is-loading="isLoading" />
    </div>
  </section>
</template>

<script>
  import Instance from '@/services/instance';
  import CachesList from '@/views/instances/caches/caches-list';
  import isEmpty from 'lodash/isEmpty';

  const flattenCaches = cacheData => {
    if (isEmpty(cacheData.cacheManagers)) {
      return [];
    }
    const mappend = Object.entries(cacheData.cacheManagers).flatMap(
      ([cacheManagerName, v]) => Object.keys(v.caches)
        .map(cacheName => ({cacheManager: cacheManagerName, name: cacheName, key: `${cacheManagerName}:${cacheName}`}))
    );
    return mappend.sort((c1, c2) => c1.key.localeCompare(c2.key));
  };

  export default {
    components: {CachesList},
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    data: () => ({
      isLoading: false,
      error: null,
      caches: [],
      filter: '',
    }),
    computed: {
      filteredCaches() {
        let filterFn = this.getFilterFn();
        return filterFn ? this.caches.filter(filterFn) : this.caches;
      }
    },
    methods: {
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
          filterFn = cache => cache.name.toLowerCase().includes(normalizedFilter);
        }

        return filterFn;
      }
    },
    created() {
      this.fetchCaches();
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/caches',
        parent: 'instances',
        path: 'caches',
        group: 'Data',
        component: this,
        label: 'Caches',
        order: 970,
        isEnabled: ({instance}) => instance.hasEndpoint('caches')
      });
    }
  }
</script>

