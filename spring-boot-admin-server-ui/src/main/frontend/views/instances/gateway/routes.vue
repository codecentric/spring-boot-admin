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
  <div :class="{ 'is-loading' : isLoading }">
    <sba-panel :header-sticks-below="['#navigation']" title="Routes" v-if="routes">
      <refresh-route-cache :instance="instance" @routes-refreshed="fetchRoutes" />

      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
            <span v-text="$t('instances.gateway.route.fetch_failed')" />
          </strong>
          <p v-text="error.message" />
        </div>
      </div>

      <div class="field">
        <p class="control is-expanded has-icons-left">
          <input
            class="input"
            type="search"
            v-model="routesFilterCriteria"
          >
          <span class="icon is-small is-left">
            <font-awesome-icon icon="filter" />
          </span>
        </p>
      </div>

      <routes-list :instance="instance" :is-loading="isLoading" :routes="routes" @route-deleted="fetchRoutes" />
    </sba-panel>
    <sba-panel :header-sticks-below="['#navigation']" title="Add Route">
      <add-route :instance="instance" @route-added="fetchRoutes" />
    </sba-panel>
  </div>
</template>

<script>
  import Instance from '@/services/instance';
  import {anyValueMatches, compareBy} from '@/utils/collections';
  import addRoute from './add-route';
  import refreshRouteCache from './refresh-route-cache';
  import routesList from './routes-list';

  const routeDefinitionMatches = (routeDef, keyword) => {
    if (!routeDef) {
      return false;
    }
    const predicate = value => String(value).toLowerCase().includes(keyword);
    return (routeDef.uri && anyValueMatches(routeDef.uri.toString(), predicate)) ||
      anyValueMatches(routeDef.predicates, predicate) ||
      anyValueMatches(routeDef.filters, predicate);
  };

  const routeMatches = (route, keyword) => {
    return route.route_id.toString().toLowerCase().includes(keyword) || routeDefinitionMatches(route.route_definition, keyword);
  };

  const sortRoutes = routes => {
    return [...routes].sort(compareBy(r => r.order))
  };

  export default {
    components: {
      refreshRouteCache,
      routesList,
      addRoute
    },
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    data: () => ({
      isLoading: false,
      error: null,
      _routes: [],
      routesFilterCriteria: null
    }),
    computed: {
      routes() {
        if (!this.routesFilterCriteria) {
          return sortRoutes(this.$data._routes);
        }
        const filtered = this.$data._routes.filter(route => routeMatches(route, this.routesFilterCriteria.toLowerCase()));
        return sortRoutes(filtered);
      }
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
          this.$data._routes = response.data
        } catch (error) {
          console.warn('Fetching routes failed:', error);
          this.error = error;
        }
        this.isLoading = false;
      }
    }
  }
</script>

