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
  <div :class="{ 'is-loading' : !hasLoaded }">
    <div v-if="error" class="message is-danger">
      <div class="message-body">
        <strong>
          <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"/>
          Fetching gateway routes failed.
        </strong>
        <p v-text="error.message"/>
      </div>
    </div>

    <sba-panel :header-sticks-below="['#navigation']" title="Routes" v-if="hasLoaded">
      <sba-confirm-button class="button refresh-button is-light"
                          :class="{'is-loading' : clearRoutesCacheStatus === 'executing', 'is-danger' : clearRoutesCacheStatus === 'failed', 'is-info' : clearRoutesCacheStatus === 'completed'}"
                          :disabled="clearRoutesCacheStatus === 'executing'"
                          @click="clearRoutesCache">
        <span v-if="clearRoutesCacheStatus === 'completed'">Routes cache cleared</span>
        <span v-else-if="clearRoutesCacheStatus === 'failed'">Failed</span>
        <span v-else>Clear routes cache</span>
      </sba-confirm-button>

      <div class="field has-addons" v-if="routes">
        <p class="control is-expanded">
          <input class="input" type="search" placeholder="routes filter" v-model="routesFilter">
        </p>
        <div class="control">
          <div class="select">
            <select v-model="sort">
              <option value="undefined">- Sort by -</option>
              <option value="route_id">Route Id</option>
              <option value="order">Order</option>
            </select>
          </div>
        </div>
      </div>

      <table class="table routes is-fullwidth is-hoverable">
        <thead>
          <th>Route Id</th>
          <th>Order</th>
          <th></th>
        </thead>
        <tbody>
        <template v-for="route in routes">
          <tr class="is-selectable" :key="route.route_id"
              @click="showDetails[route.route_id] ? $delete(showDetails, route.route_id) : $set(showDetails, route.route_id, true)">
              <td class="is-breakable">
                <span v-text="route.route_id"/>
              </td>
              <td>
                <span v-text="route.order"/>
              </td>
              <td class="routes__delete-action">
                <button class="button is-danger" :data-route_id="route.route_id"
                  v-confirm="{ ok: deleteRoute, cancel: closeDeleteDialog, message: 'Are you sure you want to delete route ' + route.route_id + '?' }">
                  Delete
                </button>
              </td>
            </tr>
            <tr v-if="showDetails[route.route_id]" :key="`${route.route_id}-detail`">
              <table class="table is-fullwidth is-hoverable">
                <tr v-for="predicate in route.route_definition.predicates" :key="predicate.name">
                  <td colspan="1" class="has-background-white-ter"><span v-text="predicate.name"/></td>
                  <td v-for="filter in route.route_definition.filters" :key="filter.name">
                    <span v-text="filter.name"/>
                  </td>
                  <td colspan="1" class="has-background-white-ter"><span v-text="route.route_definition.uri"/></td>
                  <td colspan="1" class="has-background-white-ter"><div id="container"></div></td>
                </tr>
              </table>
            </tr>
          </template>
        </tbody>
      </table>
    </sba-panel>
  </div>
</template>

<script>
  import Instance from '@/services/instance';
  import {from, listen} from '@/utils/rxjs';
  import uniqBy from 'lodash/uniqBy';
  
  const filterRoutesByKeyword = (route, keyword) => {
    return route.route_id.toString().toLowerCase().includes(keyword)
      || route.route_definition.uri.toString().toLowerCase().includes(keyword)
      || route.route_definition.predicates.filter(p => p.name.toLowerCase().includes(keyword)).length > 0
      || route.route_definition.predicates.filter(p => Object.values(p.args).filter(pv => pv.toLowerCase().includes(keyword)).length > 0).length > 0
      || route.route_definition.filters.filter(f => f.name.toLowerCase().includes(keyword)).length > 0
      || route.route_definition.filters.filter(f => Object.values(f.args).filter(av => av.toLowerCase().includes(keyword)).length > 0).length > 0;
  };

  const sortRoutes = (globalFilters, sort) => {
    if (sort === 'route_id') {
      return globalFilters.slice().sort(function(a, b) {return a.route_id.localeCompare(b.route_id)})
    } else if (sort === 'order') {
      return globalFilters.slice().sort(function(a, b) {return a.order - b.order})
    }

    return globalFilters;
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
      routesData: null,
      routesFilter: null,
      sort: 'undefined',
      showDetails: {},
      clearRoutesCacheStatus: null
    }),
    computed: {
      routes() {
        if (!this.routesData) {
          return [];
        }
        if (!this.routesFilter) {
          return sortRoutes(this.routesData, this.sort);
        }
        return sortRoutes(this.routesData.filter(route => !this.routesFilter || filterRoutesByKeyword(route, this.routesFilter.toLowerCase())), this.sort);
      }
    },
    created() {
      this.fetchRoutesData();
    },
    methods: {
      async fetchRoutesData() {
        this.error = null;
        try {
          const res = await this.instance.fetchRoutesData();
          this.routesData = uniqBy(res.data, 'route_id');
        } catch (error) {
          console.warn('Fetching routes failed:', error);
          this.error = error;
        }
        this.hasLoaded = true;
      },
      getFilterFn() {
        if (!this.filter || this.filter === '') {
          return () => true;
        }
        const regex = new RegExp(this.filter, 'i');
        return route => (route.route_id.match(regex));
      },
      deleteRoute(dialog) {
        let button = dialog.node;
        let routeId = button.dataset.route_id;
        
        try {
          this.instance.deleteRoute(routeId);
        } catch (error) {
          console.warn('Deleting route failed:', error);
          this.error = error;
        }

        dialog.close();
      },
      clearRoutesCache() {
        console.warn('clearRoutesCache');
        const vm = this;
        from(vm.instance.clearRoutesCache())
          .pipe(listen(status => vm.clearRoutesCacheStatus = status))
          .subscribe({
            complete: () => {
            setTimeout(() => vm.clearRoutesCacheStatus = null, 2500);
            return vm.$emit('reset');
          },
          error: () => vm.$emit('reset')
        });

        try {
          this.instance.clearRoutesCache();
        } catch (error) {
          console.warn('Clearing routes cache failed:', error);
          this.error = error;
        }
      },
      closeDeleteDialog: function() {
        // Dialog will get closed
      }
    }
  }
</script>

<style lang="scss">
  table .routes {
    &__delete-action {
      text-align: right;
      vertical-align: middle;
    }
  }

  .refresh-button {
    margin-bottom: 16px;
  }
</style>

