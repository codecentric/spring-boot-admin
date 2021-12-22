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
        <th v-html="$t('instances.caches.name')" />
        <th v-html="$t('instances.caches.cache_manager')" />
        <th class="is-narrow">
          <sba-action-button-scoped :instance-count="application.instances.length" :action-fn="clearCaches">
            <template v-slot="slotProps">
              <span v-if="slotProps.refreshStatus === 'completed'" v-text="$t('term.execution_successful')" />
              <span v-else-if="slotProps.refreshStatus === 'failed'" v-text="$t('term.execution_failed')" />
              <span v-else>
                <font-awesome-icon icon="trash" />&nbsp;
                <span v-text="$t('term.clear')" />
              </span>
            </template>
          </sba-action-button-scoped>
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
                  :class="{ 'is-loading' : clearing[cache.key] === 'executing', 'is-info' : clearing[cache.key] === 'completed', 'is-danger' : clearing[cache.key] === 'failed' }"
                  :disabled="cache.key in clearing" @click="clearCache(cache)"
          >
            <span v-if="clearing[cache.key] === 'completed'" v-text="$t('term.cleared')" />
            <span v-else-if="clearing[cache.key] === 'failed'" v-text="$t('term.failed')" />
            <span v-else>
              <font-awesome-icon icon="trash" />
              <span v-text="$t('term.clear')" />
            </span>
          </button>
        </td>
      </tr>
      <tr v-if="caches.length === 0">
        <td class="is-muted" colspan="3 ">
          <p v-if="isLoading" class="is-loading" v-text="$t('instances.caches.loading')" />
          <p v-else v-text="$t('instances.caches.no_caches_found')" />
        </td>
      </tr>
    </tbody>
  </table>
</template>
<script>
import Instance from '@/services/instance';
import {concatMap, listen, of, tap} from '@/utils/rxjs';
import SbaActionButtonScoped from '@/components/sba-action-button-scoped';
import Application from '@/services/application';

export default {
  name: 'CachesList',
  components: {SbaActionButtonScoped},
  props: {
    caches: {
      type: Array,
      default: () => []
    },
    instance: {
      type: Instance,
      required: true
    },
    application: {
      type: Application,
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
    clearCaches(scope) {
      if (scope === 'instance') {
        return this.instance.clearCaches();
      } else {
        return this.application.clearCaches();
      }
    },
    clearCache(cache) {
      this._clearCache(cache)
        .pipe(listen(status => {
          this.clearing = {
            ...this.clearing,
            [cache.key]: status
          }
        }))
        .subscribe({
          complete: () => {
            setTimeout(() => {
              delete this.clearing[cache.key];
              this.clearing = {...this.clearing};
            }, 2500);
            return this.$emit('cleared', cache.key);
          },
        });
    },
    _clearCache(cache) {
      return of(cache)
        .pipe(
          concatMap(async (cache) => {
            await this.instance.clearCache(cache.name, cache.cacheManager);
            return cache.key;
          }),
          tap({
            error: error => {
              console.warn(`Clearing cache ${cache.key} failed:`, error);
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
