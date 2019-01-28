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
  <sba-panel :header-sticks-below="['#navigation']" title="Add Route">
    <div v-if="error" class="message is-danger">
      <div class="message-body">
        <strong>
          <font-awesome-icon class="has-text-warning" icon="exclamation-triangle" />
          Adding route failed:
        </strong>
        <p v-text="error" />
      </div>
    </div>
    <div class="field has-addons">
      <p class="control is-expanded">
        <input class="input" placeholder="Route id" v-model="addRouteData.id" required>
      </p>
    </div>
    <div class="field has-addons">
      <p class="control is-expanded">
        <textarea rows="4" class="input" placeholder="Predicates" v-model="addRouteData.predicates" required />
      </p>
    </div>
    <div class="field has-addons">
      <p class="control is-expanded">
        <textarea rows="4" class="input" placeholder="Filters" v-model="addRouteData.filters" />
      </p>
    </div>
    <div class="field has-addons">
      <p class="control is-expanded">
        <input class="input" placeholder="Uri" v-model="addRouteData.uri" required>
      </p>
    </div>
    <div class="field has-addons">
      <p class="control is-expanded">
        <input class="input" placeholder="Order" v-model="addRouteData.order" type="number" required>
      </p>
    </div>
    <div class="field is-grouped is-grouped-right">
      <div class="control">
        <button class="button is-primary" :disabled="!addRouteData" @click="addRoute">
          <span>Add route</span>
        </button>
      </div>
    </div>
  </sba-panel>
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
      addRouteData: {
        'id': null,
        'predicates': null,
        'filters': null,
        'uri': null,
        'order': null
      }
    }),
    methods: {
      addRoute() {
        const vm = this;
        this.addRouteData.predicates = JSON.parse(vm.addRouteData.predicates);
        this.addRouteData.filters = JSON.parse(vm.addRouteData.filters);
        from(vm.instance.addGatewayRoute(vm.addRouteData))
          .subscribe({
            complete: () => {
              vm.addRouteData = {
                'id': null,
                'predicates': null,
                'filters': null,
                'uri': null,
                'order': null
              };
              vm.error = null;
              setTimeout(() => vm.$emit('route-added'), 2500);
            },
            error: (error) => {
              this.error = 'Server returned: ' + error.response.status;
            }
          });
      }
    }
  }
</script>

