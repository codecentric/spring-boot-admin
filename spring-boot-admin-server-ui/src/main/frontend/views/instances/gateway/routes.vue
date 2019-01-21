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
    <sba-panel :header-sticks-below="['#navigation']" title="Routes" v-if="routes">
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
            {{ error.action }}
          </strong>
          <p v-text="error.value.message" />
        </div>
      </div>
      <sba-confirm-button class="button refresh-button is-light"
                          :class="{'is-loading' : clearRoutesCacheStatus === 'executing', 'is-danger' : clearRoutesCacheStatus === 'failed', 'is-info' : clearRoutesCacheStatus === 'completed'}"
                          :disabled="clearRoutesCacheStatus === 'executing'"
                          @click="clearRoutesCache"
      >
        <span v-if="clearRoutesCacheStatus === 'completed'">
          Routes cache cleared
        </span>
        <span v-else-if="clearRoutesCacheStatus === 'failed'">
          Failed
        </span>
        <span v-else>
          Clear routes cache
        </span>
      </sba-confirm-button>

      <div class="field has-addons" v-if="routes">
        <p class="control is-expanded">
          <input class="input" type="search" placeholder="Search routes by name" v-model="routesFilter">
        </p>
        <div class="control">
          <div class="select">
            <select v-model="sort">
              <option value="undefined">
                - Sort by -
              </option>
              <option value="route_id">
                Route Id
              </option>
              <option value="order">
                Order
              </option>
            </select>
          </div>
        </div>
      </div>

      <table class="table routes is-fullwidth is-hoverable">
        <thead>
          <th>Route Id</th>
          <th>Order</th>
          <th />
        </thead>
        <tbody>
          <template v-for="route in routes" @route-added="fetchRoutesData" @route-deleted="fetchRoutesData">
            <tr class="is-selectable" :key="route.route_id"
                @click="showDetails[route.route_id] ? $delete(showDetails, route.route_id) : $set(showDetails, route.route_id, true)"
            >
              <td class="is-breakable">
                <span v-text="route.route_id" />
              </td>
              <td>
                <span v-text="route.order" />
              </td>
              <td class="routes__delete-action">
                <sba-confirm-button class="button refresh-button is-light"
                                    :class="{'is-loading' : findDeleteRouteStatus(route.route_id).status === 'executing', 'is-danger' : findDeleteRouteStatus(route.route_id).status === 'failed'}"
                                    :disabled="findDeleteRouteStatus(route.route_id).status === 'executing'"
                                    @click="deleteRoute(route.route_id)"
                >
                  <span v-if="findDeleteRouteStatus(route.route_id).status === 'failed'">
                    Failed
                  </span>
                  <span v-else>
                    Delete route
                  </span>
                </sba-confirm-button>
              </td>
            </tr>
            <route-detail-control :route="route" :show-details="showDetails" :key="`${route.route_id}-detail`" />
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
  import find from 'lodash/find';
  import routeDetailControl from './route-details';
  import {compareBy} from '@/utils/collections';

  const filterRoutesByKeyword = (route, keyword) => {
    return route.route_id.toString().toLowerCase().includes(keyword)
      || route.route_definition.uri.toString().toLowerCase().includes(keyword)
      || route.route_definition.predicates.some(p => p.name.toLowerCase().includes(keyword))
      || route.route_definition.predicates.some(p => Object.values(p.args).some(pv => pv.toLowerCase().includes(keyword)))
      || route.route_definition.filters.some(f => f.name.toLowerCase().includes(keyword))
      || route.route_definition.filters.some(f => Object.values(f.args).some(av => av.toLowerCase().includes(keyword)));
  };

  const sortRoutes = (routes, sort) => {
    if (sort === 'route_id') {
      return [...routes].sort(compareBy(r => r.route_id.toLowerCase()))
    } else if (sort === 'order') {
      return [...routes].sort(compareBy(r => r.order))
    }
    return routes;
  };

  export default {
    components: {routeDetailControl},
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
      clearRoutesCacheStatus: null,
      routesDeleteStatus: [{
        id: null,
        status: null,
      }]
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
          this.routesData.map(route => {
            let routeDeleteStatus = {
              id: route.route_id,
              status: null
            }
            this.routesDeleteStatus.push(routeDeleteStatus);
          });
        } catch (error) {
          console.warn('Fetching routes failed:', error);
          this.error = {action:'Fetching gateway routes failed:', value:error};
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
      deleteRoute(routeId) {
        const vm = this;
        from(vm.instance.deleteRoute(routeId))
          .pipe(listen(status => vm.findDeleteRouteStatus(routeId).status = status))
          .subscribe({
            complete: () => {
              setTimeout(() => vm.findDeleteRouteStatus(routeId).status = null, 2500);
              return vm.$emit('reset');
            },
            error: () => vm.$emit('reset')
          });
      },
      findDeleteRouteStatus(routeId) {
        return find(this.routesDeleteStatus, route => route.id === routeId);
      },
      clearRoutesCache() {
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
  .dg-btn--ok {
    background-color: #ff4949;
    border-color: transparent;
    color: #fff;
  }

  .dg-btn--cancel {
    background-color: #38d1a0;
    border-color: transparent;
    color: white;
  }
 
  .dg-btn-loader .dg-circle {
      background-color: green;
  }
  .refresh-button {
    margin-bottom: 16px;
  }
</style>
