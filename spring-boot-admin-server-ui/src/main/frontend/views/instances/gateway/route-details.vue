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
  <tr v-if="showDetails[route.route_id]" :key="`${route.route_id}-detail`">
    <td colspan="3" class="route-detail-color">
      <div class="route-detail route-detail-dimension">
        <div class="route-detail-border">
          <div class="route-detail-margin">
            <span>
              <font-awesome-icon icon="search" />
              <b>Predicates</b>
            </span>
          </div>
          <div class="route-detail-background" v-for="predicate in route.route_definition.predicates"
               :key="predicate.name"
          >
            <span class="route-detail-text" v-text="predicate.name" />
            <div class="route-detail-separator" />
            <ul>
              <li v-for="item in transformArgs(predicate.args)" :key="item">
                <span class="route-detail-arg-text" v-text="item" />
              </li>
            </ul>
          </div>
        </div>
      </div>
      <div class="route-detail route-detail-arrow">
        <font-awesome-icon icon="angle-double-right" class="route-detail-arrow" />
      </div>
      <div class="route-detail route-detail-dimension" v-if="route.route_definition.filters.length > 0">
        <div class="route-detail-border">
          <div class="route-detail-margin">
            <span>
              <font-awesome-icon icon="filter" />
              <b>Filters</b>
            </span>
          </div>
          <div v-for="filter in route.route_definition.filters" :key="filter.name"
               class="route-detail-background"
          >
            <span class="route-detail-text" v-text="filter.name" />
            <div class="route-detail-separator" />
            <ul>
              <li v-for="item in transformArgs(filter.args)" :key="item">
                <span class="route-detail-arg-text" v-text="item" />
              </li>
            </ul>
          </div>
        </div>
      </div>
      <div class="route-detail route-detail-arrow" v-if="route.route_definition.filters.length > 0">
        <font-awesome-icon icon="angle-double-right" class="route-detail-arrow" />
      </div>
      <div class="route-detail route-detail-dimension">
        <div class="route-detail-border">
          <div class="route-detail-margin">
            <span>
              <font-awesome-icon icon="map-marker" />
              <b>URI</b>
            </span>
          </div>
          <div class="route-detail-background">
            <span v-text="route.route_definition.uri" class="route-detail-text" />
          </div>
        </div>
      </div>
    </td>
  </tr>
</template>

<script>
  export default {
    props: ['route', 'showDetails'],
    methods: {
      transformArgs(args) {
        return Object.entries(args).map(([key,value])=>key + ' : ' + value);
      }
    }
  }
</script>

<style scoped>
  .route-detail {
    display: table-cell;
    vertical-align: middle;
  }

  .route-detail-dimension {
    min-width: 12em;
    max-width: 28em;
  }

  .route-detail-color {
    background-color: #fafafa;
  }

  .route-detail-arrow {
    min-width: 1em;
    max-width: 3.5em;
    font-size: 2em;
  }

  .route-detail-border {
    border: solid 1px lightgrey;
    margin: 16px;
    border-radius: 8px;
  }

  .route-detail-separator {
    border-bottom: 1px dotted #fafafa;
  }

  .route-detail-text {
    color: white;
    font-weight: bold;
  }

  .route-detail-arg-text {
    color: white;
    font-size: 12px
  }

  .route-detail-background {
    background-color: #42d3a5;
    margin: 4px;
    padding: 4px;
    border-radius: 4px;
  }

  .route-detail-margin {
    margin: 4px;
    padding: 4px;
  }

  .route-image {
    height: 16px;
    margin-right: 2px
  }
</style>
