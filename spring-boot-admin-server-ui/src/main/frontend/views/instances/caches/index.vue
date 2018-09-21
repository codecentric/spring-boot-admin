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
  <section class="section" :class="{ 'is-loading' : !hasLoaded }">
    <div class="container" v-if="hasLoaded">
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"/>
            Fetching caches failed.
          </strong>
          <p v-text="error.message"/>
        </div>
      </div>
      <p v-if="!hasCaches" class="is-muted">
        No caches found.
      </p>
      <template v-if="hasCaches">
        <div class="field">
          <div class="control">
            <sba-confirm-button class="button is-danger"
                                :class="{'is-loading' : clearAllCachesStatus === 'executing', 'is-danger' : clearAllCachesStatus === 'failed', 'is-info' : clearAllCachesStatus === 'completed'}"
                                :disabled="clearAllCachesStatus === 'executing'"
                                @click="clearCaches">
              <span v-if="clearAllCachesStatus === 'completed'">All caches cleared</span>
              <span v-else-if="clearAllCachesStatus === 'failed'">Failed</span>
              <span v-else>Clear all caches</span>
            </sba-confirm-button>
          </div>
        </div>
        <div class="field-body">
          <div class="field has-addons">
            <p class="control is-expanded">
              <input class="input" type="search" placeholder="name filter" v-model="filter">
            </p>
            <p class="control">
              <span class="button is-static">
                <span v-text="filteredCaches.length"/>
                /
                <span v-text="caches.length"/>
              </span>
            </p>
          </div>
        </div>
      </template>
      <table v-if="hasCaches" class="table is-hoverable is-fullwidth">
        <thead>
          <tr>
            <th>Name</th>
            <th>Cache manager</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="cache in limitedCaches" :key="cache.key">
            <td>
              <span class="is-breakable" v-text="cache.name"/>
              <span class="has-text-danger" v-if="cache.key in failed">
                <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"/>
                <span v-text="`Clearing failed.`"/>
              </span>
            </td>
            <td>
              <span class="is-breakable" v-text="cache.cacheManager"/>
            </td>
            <td>
              <button class="button is-primary is-pulled-right" @click="clearCache(cache)">Clear</button>
            </td>
          </tr>
          <tr v-if="limitedCaches.length === 0">
            <td class="is-muted" colspan="5">No caches found.
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script>
  import Instance from '@/services/instance';
  import _ from 'lodash';
  import {from, listen} from '@/utils/rxjs';

  const flattenCaches = cacheData => {
    return _.flatMap(Object.entries(cacheData.cacheManagers),
      ([cacheManagerName, v]) => Object.keys(v.caches).map(
        cacheName => {return {cacheManager: cacheManagerName, name: cacheName, key: cacheManagerName + ':' + cacheName};} ))
      .sort((c1,c2) => c1.key.localeCompare(c2.key));
  };

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    data: () => ({
      hasLoaded: false,
      error: null,
      caches: null,
      filter: '',
      visibleLimit: 25,
      loading: {},
      failed: {},
      clearAllCachesStatus: null,
    }),
    computed: {
      hasCaches: function () {
        return this.caches && this.caches.length;
      },
      limitedCaches() {
        if (this.visibleLimit > 0) {
          return this.filteredCaches.slice(0, this.visibleLimit);
        } else {
          return this.filteredCaches;
        }
      },
      filteredCaches() {
        let filterFn = this.getFilterFn();
        return filterFn ? this.caches.filter(filterFn) : this.caches;
      }
    },
    methods: {
      clearCaches() {
        const vm = this;
        from(vm.instance.clearCaches())
          .pipe(listen(status => vm.clearAllCachesStatus = status))
          .subscribe({
            complete: () => {
              setTimeout(() => vm.clearAllCachesStatus = null, 2500);
              return vm.$emit('reset');
            },
            error: () => vm.$emit('reset')
          });
      },
      async clearCache(cache) {
        const vm = this;
        const setLoadingHandle = setTimeout(() => vm.$set(vm.loading, cache.key, true), 150);
        try {
          await this.instance.clearCache(cache.name, cache.cacheManager);
          vm.$delete(vm.failed, cache.key, true)
        } catch (error) {
          console.warn(`Clearing cache '${cache.name}' in cacheManager '${cache.cacheManager}' failed:`, error);
          vm.$set(vm.failed, cache.key, true);
        }

        try {
          await this.fetchCaches();
        } finally {
          vm.$delete(vm.loading, cache.key);
          clearTimeout(setLoadingHandle);
        }
      },
      fetchCaches: async function () {
        this.error = null;
        try {
          const res = await this.instance.fetchCaches();
          this.caches = flattenCaches(res.data);
        } catch (error) {
          console.warn('Fetching caches failed:', error);
          this.error = error;
        }
        this.hasLoaded = true;
      },
      onScroll() {
        if (this.caches && this.$el.getBoundingClientRect().bottom - 400 <= window.innerHeight && this.visibleLimit < this.filteredCaches.length) {
          this.visibleLimit += 25;
        }
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
    mounted() {
      window.addEventListener('scroll', this.onScroll);
    },
    beforeDestroy() {
      window.removeEventListener('scroll', this.onScroll);
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/caches',
        parent: 'instances',
        path: 'caches',
        component: this,
        label: 'Caches',
        order: 970,
        isEnabled: ({instance}) => instance.hasEndpoint('caches')
      });
    }
  }
</script>

<style lang="scss">
</style>
