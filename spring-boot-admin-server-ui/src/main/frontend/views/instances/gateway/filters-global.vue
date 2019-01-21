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
          <FontAwesomeIcon class="has-text-danger" icon="exclamation-triangle" />
          Fetching global filters failed.
        </strong>
        <p v-text="error.message" />
      </div>
    </div>

    <sba-panel :header-sticks-below="['#navigation']" title="Global filters" v-if="hasLoaded">
      <div class="field has-addons" v-if="hasGlobalFiltersData">
        <p class="control is-expanded">
          <input class="input" type="search" placeholder="Search filters by name" v-model="globalFilterSearch">
        </p>
        <div class="control">
          <div class="select">
            <select v-model="sort">
              <option value="undefined">
                - Sort by -
              </option>
              <option value="name">
                Name
              </option>
              <option value="order">
                Order
              </option>
            </select>
          </div>
        </div>
      </div>

      <table class="table is-fullwidth is-hoverable" v-if="globalFilters.length > 0">
        <thead>
          <th>Filter name</th>
          <th>Order</th>
        </thead>
        <tbody>
          <tr v-for="filter in globalFilters" :key="filter.name">
            <td>
              <span v-text="filter.name" class="is-breakable" /><br>
            </td>
            <td v-text="filter.order" />
          </tr>
        </tbody>
      </table>
    </sba-panel>
  </div>
</template>

<script>
  import Instance from '@/services/instance';
  import {compareBy} from '@/utils/collections';

  const globalFilterHasKeyword = (globalFilter, keyword) => {
    return globalFilter.name.toString().toLowerCase().includes(keyword);
  };

  const sortGlobalFilter = (globalFilters, sort) => {
    if (sort === 'name') {
      return [...globalFilters].sort(compareBy(f => f.name))
    } else if (sort === 'order') {
      return [...globalFilters].sort(compareBy(f => f.order))
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
      globalFiltersData: null,
      globalFilterSearch: null,
      sort: 'undefined'
    }),
    computed: {
      hasGlobalFiltersData() {
        return this.globalFiltersData && this.globalFiltersData.length;
      },
      globalFilters() {
        if (!this.globalFiltersData) {
          return [];
        }
        if (!this.globalFilterSearch) {
          return sortGlobalFilter(this.globalFiltersData, this.sort);
        }
        return sortGlobalFilter(this.globalFiltersData.filter(globalFilter => !this.globalFilterSearch || globalFilterHasKeyword(globalFilter, this.globalFilterSearch.toLowerCase())), this.sort);
      }
    },
    created() {
      this.fetchGlobalFiltersData();
    },
    methods: {
      async fetchGlobalFiltersData() {
        this.error = null;
        try {
          const res = await this.instance.fetchGlobalFiltersData();
          this.globalFiltersData = Object.keys(res.data)
            .map(function (key) {
              return {name: key.substr(0, key.indexOf('@')), order: res.data[key]};
            });
        } catch (error) {
          console.warn('Fetching global filters failed:', error);
          this.error = error;
        }
        this.hasLoaded = true;
      }
    }
  }
</script>
