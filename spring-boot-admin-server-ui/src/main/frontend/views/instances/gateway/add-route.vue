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
          <font-awesome-icon class="has-text-warning" icon="exclamation-triangle" />
          <span v-text="$t('instances.gateway.route.adding_failed')" />
        </strong>
        <p v-text="error.message" />
      </div>
    </div>

    <div class="field">
      <label class="label" for="routeId" v-text="$t('instances.gateway.route.id')" />
      <p class="control">
        <input class="input" id="routeId" v-model="routeId" required>
      </p>
    </div>

    <div class="field">
      <label class="label" for="order" v-text="$t('instances.gateway.route.order')" />
      <p class="control">
        <input class="input" id="order" placeholder="0" v-model="routeOrder" type="number">
      </p>
    </div>

    <div class="field">
      <label class="label" for="predicates" v-text="$t('instances.gateway.route.predicates')" />
      <p class="control">
        <textarea rows="4" class="textarea" id="predicates" placeholder="[]" v-model="routePredicates" required />
      </p>
    </div>

    <div class="field">
      <label class="label" for="filters" v-text="$t('instances.gateway.route.filters')" />
      <p class="control">
        <textarea rows="4" class="textarea" id="filters" placeholder="[]" v-model="routeFilters" />
      </p>
    </div>

    <div class="field">
      <label class="label" for="routeUri" v-text="$t('instances.gateway.route.uri')" />
      <p class="control">
        <input class="input" id="routeUri" placeholder="http://example.com" v-model="routeUri" required>
      </p>
    </div>

    <div class="field is-grouped is-grouped-right">
      <div class="control">
        <button class="button is-primary" :disabled="!isAddingRoutePossible" @click="addRoute" v-text="$t('instances.gateway.route.add_route')" />
      </div>
    </div>
  </div>
</template>

<script>
  import Instance from '@/services/instance';
  import {from} from '@/utils/rxjs';

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    data: () => ({
      error: null,
      routeId: null,
      routePredicates: null,
      routeFilters: null,
      routeUri: null,
      routeOrder: null
    }),
    computed: {
      isAddingRoutePossible() {
        return this.routeId && this.routePredicates && this.routeUri;
      }
    },
    methods: {
      addRoute() {
        const vm = this;
        const newRoute = {
          id: vm.routeId,
          predicates: vm.routePredicates ? JSON.parse(vm.routePredicates) : undefined,
          filters: vm.routeFilters ? JSON.parse(vm.routeFilters) : [],
          uri: vm.routeUri,
          order: vm.routeOrder || 0
        };
        from(vm.instance.addGatewayRoute(newRoute))
          .subscribe({
            complete: () => {
              vm.routeId = null;
              vm.routePredicates = null;
              vm.routeFilters = null;
              vm.routeUri = null;
              vm.routeOrder = null;
              vm.error = null;
              setTimeout(() => vm.$emit('route-added'), 2500);
            },
            error: error => {
              this.error = error;
            }
          });
      }
    }
  }
</script>

