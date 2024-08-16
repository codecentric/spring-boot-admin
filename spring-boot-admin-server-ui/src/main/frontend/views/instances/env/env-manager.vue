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
  <sba-panel :title="$t('instances.env.manager')">
    <div
      v-for="(prop, index) in managedProperties"
      :key="`managed-${index}`"
      class="flex gap-2 pb-2"
    >
      <sba-input
        v-model="prop.name"
        :error="prop.validation"
        :list="allPropertyNames"
        :name="prop.name || 'new-prop-name'"
        class="flex-1"
        placeholder="Property name"
        type="text"
        @input="handlePropertyNameChange(prop, index)"
      />
      <sba-input
        v-model="prop.input"
        :name="prop.name || 'new-prop-value'"
        class="flex-1"
        placeholder="Value"
        type="text"
        @input="prop.status = null"
      >
        <template #append>
          <span v-if="prop.status === 'executing'">
            <font-awesome-icon
              :icon="['fas', 'sync-alt']"
              class="animate-spin"
            />
          </span>
          <span v-else-if="prop.status === 'failed'">
            <font-awesome-icon icon="exclamation-triangle" />
          </span>
          <span
            v-else-if="prop.status === 'completed' || prop.input === prop.value"
          >
            <font-awesome-icon icon="check" />
          </span>
          <span v-else-if="prop.input !== prop.value">
            <font-awesome-icon icon="pencil-alt" />
          </span>
        </template>
      </sba-input>
    </div>

    <div class="flex gap-2 justify-end items-start">
      <sba-toggle-scope-button
        v-if="application.instances.length > 1"
        v-model="scope"
        :instance-count="application.instances.length"
      />

      <sba-confirm-button
        :disabled="!hasManagedProperty || resetStatus === 'executing'"
        class="button is-light"
        @click="resetEnvironment"
      >
        <span
          v-if="resetStatus === 'completed'"
          v-text="$t('instances.env.context_resetted')"
        />
        <span
          v-else-if="resetStatus === 'failed'"
          v-text="$t('instances.env.context_reset_failed')"
        />
        <span v-else v-text="$t('instances.env.context_reset')" />
      </sba-confirm-button>
      <sba-confirm-button
        :disabled="
          hasErrorProperty ||
          !hasChangedProperty ||
          updateStatus === 'executing'
        "
        class="button is-primary"
        @click="updateEnvironment"
      >
        <span
          v-if="updateStatus === 'completed'"
          v-text="$t('instances.env.context_updated')"
        />
        <span
          v-else-if="updateStatus === 'failed'"
          v-text="$t('instances.env.context_update_failed')"
        />
        <span v-else v-text="$t('instances.env.context_update')" />
      </sba-confirm-button>
    </div>
  </sba-panel>
</template>

<script>
import { debounce, uniq } from 'lodash-es';

import { ActionScope } from '@/components/ActionScope';

import Application from '@/services/application';
import Instance from '@/services/instance';
import { concatMap, filter, from, listen } from '@/utils/rxjs';

export default {
  props: {
    application: {
      type: Application,
      required: true,
    },
    instance: {
      type: Instance,
      required: true,
    },
    propertySources: {
      type: Array,
      default: () => [],
    },
  },
  emits: ['refresh', 'update'],
  data: () => ({
    error: null,
    resetStatus: null,
    updateStatus: null,
    scope: ActionScope.INSTANCE,
    managedProperties: [
      {
        name: null,
        input: null,
        value: null,
        status: null,
        validation: null,
      },
    ],
  }),
  computed: {
    allPropertyNames() {
      return uniq(
        this.propertySources
          .map((ps) => (ps.properties ? Object.keys(ps.properties) : []))
          .reduce((result, names) => result.concat(names))
          .sort(),
      );
    },
    managerPropertySource() {
      return (
        this.propertySources.find((ps) => ps.name === 'manager') || {
          name: 'manager',
          properties: {},
        }
      );
    },
    hasManagedProperty() {
      return (
        this.managedProperties.findIndex((property) => !!property.name) >= 0
      );
    },
    hasChangedProperty() {
      return (
        this.managedProperties.findIndex(
          (property) => property.input !== property.value,
        ) >= 0
      );
    },
    hasErrorProperty() {
      return (
        this.managedProperties.findIndex(
          (property) => property.validation !== null,
        ) >= 0
      );
    },
  },
  watch: {
    managerPropertySource: {
      handler: 'updateManagedProperties',
      immediate: true,
    },
    managedProperties: {
      deep: true,
      handler() {
        const counts = this.managedProperties.reduce((acc, v) => {
          if (v.name) {
            acc[v.name] = (acc[v.name] || 0) + 1;
          }
          return acc;
        }, {});

        this.managedProperties.forEach((property) => {
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
      },
    },
  },
  methods: {
    handlePropertyNameChange: debounce(function (prop, idx) {
      if (prop.name && idx === this.managedProperties.length - 1) {
        this.managedProperties.push({
          name: null,
          input: null,
          value: null,
          status: null,
          validation: null,
        });
      }
    }, 250),
    updateEnvironment() {
      from(this.managedProperties)
        .pipe(
          filter(
            (property) => !!property.name && property.input !== property.value,
          ),
          listen((status) => (this.updateStatus = status)),
          concatMap((property) => {
            let target;

            if (this.scope === 'instance') {
              target = this.instance;
            } else {
              target = this.application;
            }

            return from(target.setEnv(property.name, property.input)).pipe(
              listen((status) => (property.status = status)),
              listen((status) => (this.updateStatus = status)),
            );
          }),
        )
        .subscribe({
          complete: () => {
            setTimeout(() => (this.updateStatus = null), 2500);
            return this.$emit('update');
          },
          error: () => this.$emit('update'),
        });
    },
    resetEnvironment() {
      let target;

      if (this.scope === 'instance') {
        target = this.instance;
      } else {
        target = this.application;
      }

      from(target.resetEnv())
        .pipe(listen((status) => (this.resetStatus = status)))
        .subscribe({
          complete: () => {
            this.managedProperties = [
              {
                name: null,
                input: null,
                value: null,
                status: null,
                validation: null,
              },
            ];
            setTimeout(() => (this.resetStatus = null), 2500);
            return this.$emit('refresh');
          },
          error: () => this.$emit('refresh'),
        });
    },
    updateManagedProperties(manager) {
      Object.entries(manager.properties).forEach(([name, property]) => {
        const managedProperty = this.managedProperties.find(
          (property) => property.name === name,
        );
        if (managedProperty) {
          managedProperty.value = property.value;
        } else {
          const idx = this.managedProperties.length - 1;
          this.managedProperties.splice(idx, 0, {
            name,
            input: property.value,
            value: property.value,
            status: null,
            validation: null,
          });
        }
      });
    },
  },
};
</script>
