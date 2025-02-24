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
  <div class="route-definition-container">
    <div class="route-definition">
      <div class="route-definition-header">
        <font-awesome-icon icon="search" />&nbsp;
        <span v-text="$t('instances.gateway.route.predicates')" />
      </div>
      <div
        v-for="predicate in routeDefinition.predicates"
        :key="predicate.name"
        class="route-definition-content"
      >
        <div class="route-definition-category" v-text="predicate.name" />
        <ul>
          <li
            v-for="item in transformArgs(predicate.args)"
            :key="item"
            v-text="item"
          />
        </ul>
      </div>
    </div>

    <font-awesome-icon
      icon="angle-double-right"
      class="route-definition-spacer"
    />

    <div v-if="routeDefinition.filters.length > 0" class="route-definition">
      <div class="route-definition-header">
        <font-awesome-icon icon="filter" />&nbsp;
        <span v-text="$t('instances.gateway.route.filters')" />
      </div>
      <div
        v-for="filter in routeDefinition.filters"
        :key="filter.name"
        class="route-definition-content"
      >
        <div class="route-definition-category" v-text="filter.name" />
        <ul>
          <li
            v-for="item in transformArgs(filter.args)"
            :key="item"
            v-text="item"
          />
        </ul>
      </div>
    </div>

    <font-awesome-icon
      v-if="routeDefinition.filters.length > 0"
      icon="angle-double-right"
      class="route-definition-spacer"
    />

    <div class="route-definition">
      <div class="route-definition-header">
        <font-awesome-icon icon="map-marker-alt" />&nbsp;
        <span v-text="$t('instances.gateway.route.uri')" />
      </div>
      <div class="route-definition-content" v-text="routeDefinition.uri" />
    </div>
  </div>
</template>

<script>
export default {
  props: {
    routeDefinition: {
      type: Object,
      required: true,
    },
  },
  methods: {
    transformArgs(args) {
      return Object.entries(args).map(([key, value]) => `${key} : ${value}`);
    },
  },
};
</script>

<style lang="css">
.route-definition {
  display: block;
  min-width: 12em;
  max-width: 28em;
  padding: 0.5em;
  margin: 1.25em;
  background-color: #fff;
  border-radius: 6px;
  box-shadow:
    0 2px 3px rgba(0, 0, 0, 0.1),
    0 0 0 1px rgba(0, 0, 0, 0.1);
}
.route-definition-container {
  display: flex;
  align-items: center;
}
.route-definition-spacer {
  font-size: 1.25rem;
  min-width: 1em;
  max-width: 3.5em;
}
.route-definition-header {
  font-size: 1.25rem;
}
.route-definition-content {
  background-color: #00d1b2;
  color: #fff;
  border-radius: 4px;
  padding: 0.25em;
  margin: 0.25em;
}
.route-definition-category {
  font-weight: 700;
  border-bottom: 1px solid #fafafa;
  display: block;
}
</style>
