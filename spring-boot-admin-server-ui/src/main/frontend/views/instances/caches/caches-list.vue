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
  <table class="table-auto w-full">
    <thead>
      <tr>
        <th v-html="$t('instances.caches.name')" />
        <th v-html="$t('instances.caches.cache_manager')" />
        <th>&nbsp;</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="cache in caches" :key="cache.key">
        <td>
          <span class="is-breakable" v-text="cache.name" />
        </td>
        <td>
          <span class="is-breakable" v-text="cache.cacheManager" />
        </td>
        <td class="is-narrow text-right">
          <sba-button
            class="button"
            :class="{
              'is-loading': clearing[cache.key] === 'executing',
              'is-info': clearing[cache.key] === 'completed',
              'is-danger': clearing[cache.key] === 'failed',
            }"
            :disabled="cache.key in clearing"
            @click="clearCache(cache)"
          >
            <span
              v-if="clearing[cache.key] === 'completed'"
              v-text="$t('term.cleared')"
            />
            <span
              v-else-if="clearing[cache.key] === 'failed'"
              v-text="$t('term.failed')"
            />
            <span v-else>
              <font-awesome-icon icon="trash" class="mr-2" />
              <span v-text="$t('term.clear')" />
            </span>
          </sba-button>
        </td>
      </tr>
      <tr v-if="caches.length === 0">
        <td class="is-muted" colspan="3 ">
          <p
            v-if="isLoading"
            class="is-loading"
            v-text="$t('instances.caches.loading')"
          />
          <p v-else v-text="$t('instances.caches.no_caches_found')" />
        </td>
      </tr>
    </tbody>
  </table>
</template>
<script>
import { ActionScope } from '@/components/ActionScope';

import Application from '@/services/application';
import Instance from '@/services/instance';
import { concatMap, listen, of, tap } from '@/utils/rxjs';

export default {
  name: 'CachesList',
  props: {
    caches: {
      type: Array,
      default: () => [],
    },
    instance: {
      type: Instance,
      required: true,
    },
    application: {
      type: Application,
      required: true,
    },
    isLoading: {
      type: Boolean,
      default: false,
    },
    scope: {
      type: ActionScope,
      required: true,
    },
  },
  emits: ['cleared'],
  data: () => ({
    clearing: {},
    clearingAll: null,
  }),
  methods: {
    clearCache(cache) {
      this._clearCache(cache)
        .pipe(
          listen((status) => {
            this.clearing = {
              ...this.clearing,
              [cache.key]: status,
            };
          }),
        )
        .subscribe({
          complete: () => {
            setTimeout(() => {
              delete this.clearing[cache.key];
              this.clearing = { ...this.clearing };
            }, 2500);
            return this.$emit('cleared', cache.key);
          },
        });
    },
    _clearCache(cache) {
      let scope = this.scope;
      return of(cache).pipe(
        concatMap(async (cache) => {
          if (scope === ActionScope.APPLICATION) {
            await this.application.clearCache(cache.name, cache.cacheManager);
          } else {
            await this.instance.clearCache(cache.name, cache.cacheManager);
          }
          return cache.key;
        }),
        tap({
          error: (error) => {
            console.warn(`Clearing cache ${cache.key} failed:`, error);
          },
        }),
      );
    },
  },
};
</script>
