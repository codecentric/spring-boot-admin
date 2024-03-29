<!--
  - Copyright 2014-2020 the original author or authors.
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
  <div class="field">
    <div class="control">
      <button
        :class="{
          'is-loading': refreshingRouteCache === 'executing',
          'is-danger': refreshingRouteCache === 'failed',
          'is-info': refreshingRouteCache === 'completed',
        }"
        :disabled="refreshingRouteCache === 'executing'"
        class="button is-light"
        @click="refreshRoutesCache"
      >
        <span
          v-if="refreshingRouteCache === 'completed'"
          v-text="$t('instances.gateway.route.cache_refreshed')"
        />
        <span
          v-else-if="refreshingRouteCache === 'failed'"
          v-text="$t('instances.gateway.route.cache_refresh_failed')"
        />
        <span v-else v-text="$t('instances.gateway.route.cache_refresh')" />
      </button>
    </div>
  </div>
</template>
<script>
import Instance from '@/services/instance';
import { from, listen } from '@/utils/rxjs';

export default {
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  emits: ['routes-refreshed'],
  data: () => ({
    refreshingRouteCache: null,
  }),
  methods: {
    refreshRoutesCache() {
      from(this.instance.refreshGatewayRoutesCache())
        .pipe(listen((status) => (this.refreshingRouteCache = status)))
        .subscribe({
          complete: () => {
            this.$emit('routes-refreshed');
            return setTimeout(() => (this.refreshingRouteCache = null), 2500);
          },
        });
    },
  },
};
</script>
