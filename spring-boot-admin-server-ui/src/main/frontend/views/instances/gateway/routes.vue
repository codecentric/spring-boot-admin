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
          <template>
            <tr class="is-selectable" :key="route.route_id" v-for="route in routes">
              <td class="is-breakable">
                <span v-text="route.route_id"/>
              </td>
              <td>
                <span v-text="route.order"/>
              </td>
              <td class="routes__delete-action">
                <button class="button is-danger">Delete</button>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </sba-panel>
  </div>
</template>

<script>
  import Instance from '@/services/instance';
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
      sort: 'undefined'
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
</style>

