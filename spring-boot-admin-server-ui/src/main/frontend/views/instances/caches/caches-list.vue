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
  <table class="caches table is-fullwidth">
    <thead>
      <tr>
        <th>Name</th>
        <th>Cache Manager</th>
        <th class="is-narrow">
          <sba-confirm-button
            class="button"
            :class="{'is-loading' : clearingAll === 'executing', 'is-info' : clearingAll === 'completed', 'is-danger' : clearingAll === 'failed'}"
            :disabled="clearingAll !== null"
            @click="clearCaches"
          >
            <span v-if="clearingAll === 'completed'">Cleared</span>
            <span v-else-if="clearingAll === 'failed'">Failed</span>
            <span v-else><font-awesome-icon icon="trash" />&nbsp;Clear</span>
          </sba-confirm-button>
        </th>
      </tr>
    </thead>
    <tbody>
      <tr
        v-for="cache in caches"
        :key="cache.key"
      >
        <td>
          <span
            class="is-breakable"
            v-text="cache.name"
          />
        </td>
        <td>
          <span
            class="is-breakable"
            v-text="cache.cacheManager"
          />
        </td>
        <td class="is-narrow">
          <button class="button"
                  :class="{ 'is-loading' : clearing[cache.name] === 'executing', 'is-info' : clearing[cache.name] === 'completed', 'is-danger' : clearing[cache.name] === 'failed' }"
                  :disabled="cache.name in clearing" @click="clearCache(cache.name)"
          >
            <span v-if="clearing[cache.name] === 'completed'">Cleared</span>
            <span v-else-if="clearing[cache.name] === 'failed'">Failed</span>
            <span v-else><font-awesome-icon icon="trash" />&nbsp;Clear</span>
          </button>
        </td>
      </tr>
      <tr v-if="caches.length === 0">
        <td class="is-muted" colspan="3 ">
          <p v-if="isLoading" class="is-loading">Loading Caches...</p>
          <p v-else>No caches found.</p>
        </td>
      </tr>
    </tbody>
  </table>
</template>
<script>
  import Instance from '@/services/instance';
  import {concatMap, from, listen, of, tap} from '@/utils/rxjs';


  export default {
    name: 'CachesList',
    props: {
      caches: {
        type: Array,
        default: () => []
      },
      instance: {
        type: Instance,
        required: true
      },
      isLoading: {
        type: Boolean,
        default: false
      }
    },
    data: () => ({
      clearing: {},
      clearingAll: null,
    }),
    methods: {
      clearCaches() {
        const vm = this;
        from(vm.instance.clearCaches())
          .pipe(listen(status => vm.clearingAll = status))
          .subscribe({
            complete: () => {
              setTimeout(() => vm.clearingAll = null, 2500);
              return vm.$emit('cleared', '*');
            }
          });
      },
      clearCache(cache) {
        const vm = this;
        vm._clearCache(cache)
          .pipe(listen(status => vm.$set(vm.clearing, cache, status)))
          .subscribe({
            complete: () => {
              setTimeout(() => vm.$delete(vm.clearing, cache), 2500);
              return vm.$emit('cleared', cache);
            },
          });
      },
      _clearCache(cache) {
        const vm = this;
        return of(cache)
          .pipe(
            concatMap(async cache => {
              await vm.instance.clearCache(cache);
              return cache;
            }),
            tap({
              error: error => {
                console.warn(`Clearing cache ${cache} failed:`, error);
              }
            })
          );
      }
    }
  }
</script>
<style lang="scss">
  .caches {
    td, th {
      vertical-align: middle;
    }
  }
</style>
