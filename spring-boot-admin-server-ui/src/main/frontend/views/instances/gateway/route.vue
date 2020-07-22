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
  <div class="route-container">
    <div class="route">
      <div class="route-header">
        <font-awesome-icon icon="search" />&nbsp;
        <span v-text="$t('instances.gateway.route.predicates')" />
      </div>
      <div class="route-content" v-text="route.predicate" />
    </div>

    <font-awesome-icon
      icon="angle-double-right"
      class="route-spacer"
    />

    <div class="route" v-if="route.filters.length > 0">
      <div class="route-header">
        <font-awesome-icon icon="filter" />&nbsp;
        <span v-text="$t('instances.gateway.route.filters')" />
      </div>
      <div
        class="route-content"
        v-for="filter in route.filters"
        :key="filter"
        v-text="filter"
      />
    </div>

    <font-awesome-icon
      icon="angle-double-right"
      class="route-spacer"
      v-if="route.filters.length > 0"
    />

    <div class="route">
      <div class="route-header">
        <font-awesome-icon icon="map-marker-alt" />&nbsp;
        <span v-text="$t('instances.gateway.route.uri')" />
      </div>
      <div
        class="route-content"
        v-text="route.uri"
      />
    </div>
  </div>
</template>

<script>
  export default {
    props: {
      route: {
        type: Object,
        required: true
      }
    },
    methods: {
      transformArgs(args) {
        return Object.entries(args).map(([key, value]) => `${key} : ${value}`);
      }
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .route {
    display: block;
    min-width: 12em;
    max-width: 28em;
    padding: 0.5em;
    margin: 1.25em;
    background-color: $white;
    border-radius: $radius-large;
    box-shadow: 0 2px 3px rgba($black, 0.1), 0 0 0 1px rgba($black, 0.1);

    &-container {
      display: flex;
      align-items: center;
    }

    &-spacer {
      font-size: $size-5;
      min-width: 1em;
      max-width: 3.5em;
    }

    &-header {
      font-size: $size-5;
    }

    &-content {
      background-color: $primary;
      color: $primary-invert;
      border-radius: $radius;
      padding: 0.25em;
      margin: 0.25em;
    }

    &-category {
      font-weight: 700;
      border-bottom: 1px solid $white-bis;
      display: block;
    }
  }
</style>
