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
  <div :class="{ 'is-loading': isLoading }">
    <sba-panel
      v-if="routes"
      :header-sticks-below="'#subnavigation'"
      title="Routes"
    >
      <refresh-route-cache
        :instance="instance"
        @routes-refreshed="fetchRoutes"
      />

      <sba-alert v-if="error" :error="error" :title="$t('term.fetch_failed')" />

      <div class="field">
        <p class="control is-expanded has-icons-left">
          <input v-model="routesFilterCriteria" class="input" type="search" />
          <span class="icon is-small is-left">
            <font-awesome-icon icon="filter" />
          </span>
        </p>
      </div>

      <routes-list
        :instance="instance"
        :is-loading="isLoading"
        :routes="routes"
        @route-deleted="fetchRoutes"
      />
    </sba-panel>
    <sba-panel title="Add Route">
      <add-route :instance="instance" @route-added="fetchRoutes" />
    </sba-panel>
  </div>
</template>

<script>
import Instance from '@/services/instance';
import { anyValueMatches, compareBy } from '@/utils/collections';
import addRoute from '@/views/instances/gateway/add-route';
import refreshRouteCache from '@/views/instances/gateway/refresh-route-cache';
import routesList from '@/views/instances/gateway/routes-list';

const routeDefinitionMatches = (routeDef, keyword) => {
  if (!routeDef) {
    return false;
  }
  const predicate = (value) => String(value).toLowerCase().includes(keyword);
  return (
    (routeDef.uri && anyValueMatches(routeDef.uri.toString(), predicate)) ||
    anyValueMatches(routeDef.predicates, predicate) ||
    anyValueMatches(routeDef.filters, predicate)
  );
};

const routeMatches = (route, keyword) => {
  return (
    route.route_id.toString().toLowerCase().includes(keyword) ||
    routeDefinitionMatches(route.route_definition, keyword)
  );
};

const sortRoutes = (routes) => {
  return [...routes].sort(compareBy((r) => r.order));
};

export default {
  components: {
    refreshRouteCache,
    routesList,
    addRoute,
  },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    isLoading: false,
    error: null,
    $routes: [],
    routesFilterCriteria: null,
  }),
  computed: {
    routes() {
      if (!this.routesFilterCriteria) {
        return sortRoutes(this.$data.$routes);
      }
      const filtered = this.$data.$routes.filter((route) =>
        routeMatches(route, this.routesFilterCriteria.toLowerCase()),
      );
      return sortRoutes(filtered);
    },
  },
  created() {
    this.fetchRoutes();
  },
  methods: {
    async fetchRoutes() {
      this.error = null;
      this.isLoading = true;
      try {
        const response = await this.instance.fetchGatewayRoutes();
        this.$data.$routes = response.data;
      } catch (error) {
        console.warn('Fetching routes failed:', error);
        this.error = error;
      }
      this.isLoading = false;
    },
  },
};
</script>
