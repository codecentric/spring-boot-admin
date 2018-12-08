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
  <div class="container" :class="{ 'is-loading' : !hasLoaded }">
    <div v-if="error" class="message is-danger">
      <div class="message-body">
        <strong>
          <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"/>
          Fetching gateway routes failed.
        </strong>
        <p v-text="error.message" />
      </div>
    </div>


    <sba-panel :header-sticks-below="['#navigation']" title="Routes" v-if="routes">
      <div class="field has-addons">
        <p class="control is-expanded">
          <input class="input" placeholder="Route id" v-model="addRouteData.id">
        </p>
      </div>
      <div class="field has-addons">
        <p class="control is-expanded">
          <textarea rows="4" cols="50" class="input" placeholder="Predicates" v-model="addRouteData.predicates"></textarea>
        </p>
      </div>
      <div class="field has-addons">
        <p class="control is-expanded">
          <input class="input" placeholder="Filters" v-model="addRouteData.filters">
        </p>
      </div>
      <div class="field has-addons">
        <p class="control is-expanded">
          <input class="input" placeholder="Uri" v-model="addRouteData.uri">
        </p>
      </div>
      <div class="field has-addons">
        <p class="control is-expanded">
          <input class="input" placeholder="Order" v-model="addRouteData.order">
        </p>
      </div>
      <div class="field has-addons">
        <div class="control">
          <button class="button is-primary" :disabled="!addRouteData" @click="addRoute">
            <span>Add route</span>
          </button>
        </div>
      </div>

      <div class="field has-addons" v-if="routes">
        <p class="control is-expanded">
          <input class="input" type="search" placeholder="Search routes by name" v-model="routesFilter">
        </p>
      </div>

      <table class="table is-fullwidth is-hoverable">
        <thead>
          <th>Route id</th>
          <th>Order</th>
          <th></th>
        </thead>
        <tbody>
          <template>
            <tr class="is-selectable" :key="route.route_id" v-for="route in filterRoutes">
              <td class="is-breakable">
                <span v-text="route.route_id" />
              </td>
              <td class="is-breakable">
                <span v-text="route.order" />
              </td>
              <td class="is-breakable">
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
  import {from} from '@/utils/rxjs';
  import uniqBy from 'lodash/uniqBy';

  const filterRoutesByKeyword = (route, keyword) => {
    return route.route_id.toString().toLowerCase().includes(keyword);
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
      filter: '',
      routes: null,
      routesFilter: null,
      addRouteData: {
        'id': 'idddd',
        'predicates': '[{"name":"Path","args":{"_genkey_0":"/first"}}]',
        'filters': '[]',
        'uri': 'http://example.org',
        'order': 0
      }
    }),
    computed: {
      filterRoutes() {
        if (!this.routes) {
          return [];
        }
        if (!this.routesFilter) {
          return this.routes;
        }
        return this.routes.filter(route => !this.routesFilter || filterRoutesByKeyword(route, this.routesFilter.toLowerCase()));
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
          this.routes = uniqBy(res.data, 'route_id');
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
      addRoute() {
        //secure addRouteData from empty strings (add top check if trimmed length larger than zero)
        const vm = this;
        from(vm.instance.addGatewayRoute(JSON.parse(vm.addRouteData)))
          .subscribe({
            complete: () => {
              console.warn('complete');
              //Clean addRouteData object
            },
            error: () => console.warn('error')
          });
      }
    }
  }
</script>

