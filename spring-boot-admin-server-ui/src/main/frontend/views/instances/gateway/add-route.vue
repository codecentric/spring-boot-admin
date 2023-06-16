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
  <div>
    <div v-if="error" class="message is-danger">
      <div class="message-body">
        <strong>
          <font-awesome-icon
            class="has-text-warning"
            icon="exclamation-triangle"
          />
          <span v-text="$t('instances.gateway.route.adding_failed')" />
        </strong>
        <p v-text="error.message" />
      </div>
    </div>

    <div class="field">
      <label
        class="label"
        for="routeId"
        v-text="$t('instances.gateway.route.id')"
      />
      <p class="control">
        <input id="routeId" v-model="routeId" class="input" required />
      </p>
    </div>

    <div class="field">
      <label
        class="label"
        for="order"
        v-text="$t('instances.gateway.route.order')"
      />
      <p class="control">
        <input
          id="order"
          v-model="routeOrder"
          class="input"
          placeholder="0"
          type="number"
        />
      </p>
    </div>

    <div class="field">
      <label
        class="label"
        for="predicates"
        v-text="$t('instances.gateway.route.predicates')"
      />
      <p class="control">
        <textarea
          id="predicates"
          v-model="routePredicates"
          class="textarea"
          placeholder="[]"
          required
          rows="4"
        />
      </p>
    </div>

    <div class="field">
      <label
        class="label"
        for="filters"
        v-text="$t('instances.gateway.route.filters')"
      />
      <p class="control">
        <textarea
          id="filters"
          v-model="routeFilters"
          class="textarea"
          placeholder="[]"
          rows="4"
        />
      </p>
    </div>

    <div class="field">
      <label
        class="label"
        for="routeUri"
        v-text="$t('instances.gateway.route.uri')"
      />
      <p class="control">
        <input
          id="routeUri"
          v-model="routeUri"
          class="input"
          placeholder="http://example.com"
          required
        />
      </p>
    </div>

    <div class="field is-grouped is-grouped-right">
      <div class="control">
        <button
          :disabled="!isAddingRoutePossible"
          class="button is-primary"
          @click="addRoute"
          v-text="$t('instances.gateway.route.add_route')"
        />
      </div>
    </div>
  </div>
</template>

<script>
import Instance from '@/services/instance';
import { from } from '@/utils/rxjs';

export default {
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  emits: ['route-added'],
  data: () => ({
    error: null,
    routeId: null,
    routePredicates: null,
    routeFilters: null,
    routeUri: null,
    routeOrder: null,
  }),
  computed: {
    isAddingRoutePossible() {
      return this.routeId && this.routePredicates && this.routeUri;
    },
  },
  methods: {
    addRoute() {
      const newRoute = {
        id: this.routeId,
        predicates: this.routePredicates
          ? JSON.parse(this.routePredicates)
          : undefined,
        filters: this.routeFilters ? JSON.parse(this.routeFilters) : [],
        uri: this.routeUri,
        order: this.routeOrder || 0,
      };
      from(this.instance.addGatewayRoute(newRoute)).subscribe({
        complete: () => {
          this.routeId = null;
          this.routePredicates = null;
          this.routeFilters = null;
          this.routeUri = null;
          this.routeOrder = null;
          this.error = null;
          setTimeout(() => this.$emit('route-added'), 2500);
        },
        error: (error) => {
          this.error = error;
        },
      });
    },
  },
};
</script>
