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
  <section :class="{ 'is-loading' : !hasLoaded }" class="section">
    <sba-alert v-if="error" :error="error" :title="$t('instances.env.fetch_failed')" />

    <div v-if="env && env.activeProfiles.length > 0" class="field is-grouped is-grouped-multiline">
      <div v-for="profile in env.activeProfiles" :key="profile" class="control">
        <div class="tags has-addons">
          <span class="tag is-medium is-primary" v-text="$t('instances.env.active_profile')" />
          <span class="tag is-medium" v-text="profile" />
        </div>
      </div>
    </div>
    <refresh v-if="instance.hasEndpoint('refresh')"
             :instance="instance"
             :instance-count="application.instances.length"
             :application="application"
             @reset="fetchEnv"
    />
    <sba-env-manager v-if="env && hasEnvManagerSupport"
                     :instance="instance" :property-sources="env.propertySources"
                     @refresh="fetchEnv" @update="fetchEnv"
    />
    <div v-if="env" class="field">
      <p class="control is-expanded has-icons-left">
        <input
          v-model="filter"
          class="input"
          type="search"
        >
        <span class="icon is-small is-left">
          <font-awesome-icon icon="filter" />
        </span>
      </p>
    </div>
    <sba-panel v-for="propertySource in propertySources"
               :key="propertySource.name" :header-sticks-below="['#navigation']"
               :title="propertySource.name"
    >
      <table v-if="propertySource.properties && Object.keys(propertySource.properties).length > 0"
             class="table is-fullwidth"
      >
        <tr v-for="(value, name) in propertySource.properties" :key="`${propertySource.name}-${name}`">
          <td>
            <span v-text="name" /><br>
            <small v-if="value.origin" class="is-muted" v-text="value.origin" />
          </td>
          <td class="is-breakable" v-text="getValue(name, value.value)" />
        </tr>
      </table>
      <p v-else class="is-muted" v-text="$t('instances.env.no_properties')" />
    </sba-panel>
  </section>
</template>

<script>
import Instance from '@/services/instance';
import pickBy from 'lodash/pickBy';
import {VIEW_GROUP} from '../../index';
import sbaEnvManager from './env-manager';
  import refresh from './refresh';
  import Application from '@/services/application';

const filterProperty = (needle) => (property, name) => {
  return name.toString().toLowerCase().includes(needle) || (property.value && property.value.toString().toLowerCase().includes(needle));
};
const filterProperties = (needle, properties) => pickBy(properties, filterProperty(needle));
const filterPropertySource = (needle) => (propertySource) => {
  if (!propertySource || !propertySource.properties) {
    return null;
  }
  return {
    ...propertySource,
    properties: filterProperties(needle, propertySource.properties)
  };
};

export default {
  props: {
    instance: {
      type: Instance,
      required: true
      },
      application: {
        type: Application,
        required: true
    }
  },
    components: {sbaEnvManager, refresh},
  data: () => ({
    hasLoaded: false,
    error: null,
    env: null,
    filter: null,
    hasEnvManagerSupport: false,
    propertyNamesToEscape: [
      'line.separator'
    ]
  }),
  computed: {
    propertySources() {
      if (!this.env) {
        return [];
      }
      if (!this.filter) {
        return this.env.propertySources;
      }
      return this.env.propertySources
        .map(filterPropertySource(this.filter.toLowerCase()))
        .filter(ps => ps && Object.keys(ps.properties).length > 0);
    }
  },
  created() {
    this.fetchEnv();
    this.determineEnvManagerSupport();
  },
  methods: {
    async fetchEnv() {
      this.error = null;
      try {
        const res = await this.instance.fetchEnv();
        this.env = res.data;
      } catch (error) {
        console.warn('Fetching environment failed:', error);
        this.error = error;
      }
      this.hasLoaded = true;
    },
    async determineEnvManagerSupport() {
      try {
        this.hasEnvManagerSupport = await this.instance.hasEnvManagerSupport();
      } catch (error) {
        console.warn('Determine env manager support failed:', error);
        this.hasEnvManagerSupport = false;
      }
    },
    getValue(name, value) {
      if (this.propertyNamesToEscape.includes(name)) {
        return value.replace(/\n/g, '\\n')
          .replace(/\r/g, '\\r');
      }
      return value;
    }
  },
  install({viewRegistry}) {
    viewRegistry.addView({
      name: 'instances/env',
      parent: 'instances',
      path: 'env',
      component: this,
      label: 'instances.env.label',
      group: VIEW_GROUP.INSIGHTS,
      order: 100,
      isEnabled: ({instance}) => instance.hasEndpoint('env')
    });
  }
}
</script>
