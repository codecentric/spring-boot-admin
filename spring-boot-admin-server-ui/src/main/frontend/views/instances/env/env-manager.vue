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
  <div class="box">
    <h1 class="is-size-5">Environment Manager</h1>
    <datalist id="allPropertyNames">
      <option v-for="name in allPropertyNames" :key="name" v-text="name" />
    </datalist>
    <div class="field is-horizontal" v-for="(prop, index) in managedProperties" :key="`managed-${index}`">
      <div class="field-body">
        <div class="field">
          <div class="control">
            <input class="input" type="text" placeholder="Property name" list="allPropertyNames"
                   v-model="prop.name" @input="handlePropertyNameChange(prop, index)"
            >
          </div>
          <p class="help is-danger" v-text="prop.validation" />
        </div>
        <div class="field">
          <div class="control has-icons-right" :class="{'is-loading' : prop.status === 'executing'}">
            <input class="input" type="text" placeholder="Value" v-model="prop.input"
                   @input="prop.status = null"
            >
            <span class="icon is-right has-text-success" v-if="prop.status === 'completed'">
              <font-awesome-icon icon="check" />
            </span>
            <span class="icon is-right has-text-warning" v-else-if="prop.status === 'failed'">
              <font-awesome-icon icon="exclamation-triangle" />
            </span>
            <span class="icon is-right" v-else-if="prop.input !== prop.value">
              <font-awesome-icon icon="pencil-alt" />
            </span>
          </div>
        </div>
      </div>
    </div>
    <div class="field is-horizontal">
      <div class="field-body" v-if="instance.hasEndpoint('refresh')">
        <div class="field">
          <div class="control">
            <sba-confirm-button class="button is-light"
                                :class="{'is-loading' : refreshStatus === 'executing', 'is-danger' : refreshStatus === 'failed', 'is-info' : refreshStatus === 'completed'}"
                                :disabled="refreshStatus === 'executing'"
                                @click="refreshContext"
            >
              <span v-if="refreshStatus === 'completed'">Context refreshed</span>
              <span v-else-if="refreshStatus === 'failed'">Failed</span>
              <span v-else>Refresh Context</span>
            </sba-confirm-button>
          </div>
        </div>
      </div>
      <div class="field-body">
        <div class="field is-grouped is-grouped-right">
          <div class="control">
            <button class="button is-light"
                    :class="{'is-loading' : resetStatus === 'executing', 'is-danger' : resetStatus === 'failed', 'is-success' : resetStatus === 'completed'}"
                    :disabled="!hasManagedProperty || resetStatus === 'executing'"
                    @click="resetEnvironment"
            >
              <span v-if="resetStatus === 'completed'">Resetted</span>
              <span v-else-if="resetStatus === 'failed'">Failed</span>
              <span v-else>Reset</span>
            </button>
          </div>
          <div class="control">
            <button class="button is-primary"
                    :class="{'is-loading' : updateStatus === 'executing', 'is-danger' : updateStatus === 'failed', 'is-success' : updateStatus === 'completed'}"
                    :disabled="hasErrorProperty || !hasChangedProperty || updateStatus === 'executing'"
                    @click="updateEnvironment"
            >
              <span v-if="updateStatus === 'completed'">Updated</span>
              <span v-else-if="updateStatus === 'failed'">Failed</span>
              <span v-else>Update</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import Instance from '@/services/instance';
  import {concatMap, filter, from, listen} from '@/utils/rxjs';
  import debounce from 'lodash/debounce';
  import uniq from 'lodash/uniq';


  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      },
      propertySources: {
        type: Array,
        default: () => []
      }
    },
    data: () => ({
      error: null,
      refreshStatus: null,
      resetStatus: null,
      updateStatus: null,
      managedProperties: [{
        name: null,
        input: null,
        value: null,
        status: null,
        validation: null
      }]
    }),
    computed: {
      allPropertyNames() {
        return uniq(this.propertySources.map(ps => Object.keys(ps.properties))
          .reduce((result, names) => result.concat(names))
          .sort());
      },
      managerPropertySource() {
        return this.propertySources.find(ps => ps.name === 'manager') || {name: 'manager', properties: {}};
      },
      hasManagedProperty() {
        return this.managedProperties.findIndex(property => !!property.name) >= 0;
      },
      hasChangedProperty() {
        return this.managedProperties.findIndex(property => property.input !== property.value) >= 0;
      },
      hasErrorProperty() {
        return this.managedProperties.findIndex(property => property.validation !== null) >= 0;
      }
    },
    methods: {
      handlePropertyNameChange: debounce(function (prop, idx) {
        if (prop.name && idx === this.managedProperties.length - 1) {
          this.managedProperties.push({
            name: null,
            input: null,
            value: null,
            status: null,
            validation: null
          });
        }
      }, 250),
      refreshContext() {
        const vm = this;
        from(vm.instance.refreshContext())
          .pipe(listen(status => vm.refreshStatus = status))
          .subscribe({
            complete: () => {
              setTimeout(() => vm.refreshStatus = null, 2500);
              return vm.$emit('reset');
            },
            error: () => vm.$emit('reset')
          });
      },
      updateEnvironment() {
        const vm = this;
        from(vm.managedProperties)
          .pipe(
            filter(property => !!property.name && property.input !== property.value),
            listen(status => vm.updateStatus = status),
            concatMap(
              property => from(vm.instance.setEnv(property.name, property.input))
                .pipe(listen(status => property.status = status))
            )
          )
          .subscribe({
            complete: () => {
              setTimeout(() => vm.updateStatus = null, 2500);
              return vm.$emit('update');
            },
            error: () => vm.$emit('update')
          });
      },
      resetEnvironment() {
        const vm = this;
        from(vm.instance.resetEnv())
          .pipe(listen(status => vm.resetStatus = status))
          .subscribe({
            complete: () => {
              vm.managedProperties = [{
                name: null,
                input: null,
                value: null,
                status: null,
                validation: null
              }];
              setTimeout(() => vm.resetStatus = null, 2500);
              return vm.$emit('refresh');
            },
            error: () => vm.$emit('refresh')
          });
      },
      updateManagedProperties(manager) {
        Object.entries(manager.properties).forEach(([name, property]) => {
          const managedProperty = this.managedProperties.find(property => property.name === name);
          if (managedProperty) {
            managedProperty.value = property.value
          } else {
            const idx = this.managedProperties.length - 1;
            this.managedProperties.splice(idx, 0, {
              name,
              input: property.value,
              value: property.value,
              status: null,
              validation: null
            })
          }
        });
      }
    },
    watch: {
      managerPropertySource: {
        handler: 'updateManagedProperties',
        immediate: true
      },
      managedProperties: {
        deep: true,
        handler() {
          const counts = this.managedProperties.reduce(
            (acc, v) => {
              if (v.name) {
                acc[v.name] = (acc[v.name] || 0) + 1;
              }
              return acc;
            }, {});
          this.managedProperties.forEach(property => {
            if (!property.name) {
              if (property.input) {
                property.validation = 'Property name is required';
              }
              return;
            }
            const count = counts[property.name] || 0;
            if (count > 1) {
              property.validation = 'Property name must be unique';
              return;
            }
            property.validation = null;
          });
        }
      }
    }
  }
</script>
