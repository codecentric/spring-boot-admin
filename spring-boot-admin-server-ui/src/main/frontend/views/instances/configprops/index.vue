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
  <sba-instance-section :error="error" :loading="!hasLoaded">
    <template #before>
      <sba-sticky-subnav>
        <sba-input
          v-model="filter"
          :placeholder="$t('term.filter')"
          name="filter"
          type="search"
        >
          <template #prepend>
            <font-awesome-icon icon="filter" />
          </template>
        </sba-input>
      </sba-sticky-subnav>
    </template>

    <sba-panel
      v-for="bean in configurationPropertiesBeans"
      :key="bean.name"
      :header-sticks-below="'#subnavigation'"
      :title="bean.name"
    >
      <div class="-mx-4 -my-3">
        <table
          v-if="Object.keys(bean.properties).length > 0"
          class="table-auto w-full"
        >
          <tr
            v-for="(value, name, idx) in bean.properties"
            :key="`${bean.name}-${name}`"
            :class="{ 'bg-gray-50': idx % 2 === 0 }"
          >
            <td class="w-1/2 px-4 py-3" v-text="name" />
            <td class="px-4 py-3" v-text="value" />
          </tr>
        </table>
      </div>
    </sba-panel>
  </sba-instance-section>
</template>

<script>
import { isEmpty, mapKeys, pickBy } from 'lodash-es';

import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

const filterProperty = (needle) => (value, name) => {
  return (
    name.toString().toLowerCase().includes(needle) ||
    (value && value.toString().toLowerCase().includes(needle))
  );
};
const filterProperties = (needle, properties) =>
  pickBy(properties, filterProperty(needle));
const filterConfigurationProperties = (needle) => (propertySource) => {
  if (!propertySource || !propertySource.properties) {
    return null;
  }
  return {
    ...propertySource,
    properties: filterProperties(needle, propertySource.properties),
  };
};

function flattenBean(obj, prefix = '') {
  if (Object(obj) !== obj) {
    return { [prefix]: obj };
  }

  if (Array.isArray(obj)) {
    if (obj.length === 0) {
      return { [prefix]: [] };
    } else {
      return obj
        .map((value, idx) => flattenBean(value, `${prefix}[${idx}]`))
        .reduce((c, n) => ({ ...c, ...n }), {});
    }
  } else {
    if (isEmpty(obj)) {
      return { [prefix]: {} };
    } else {
      return Object.entries(obj)
        .map(([name, value]) =>
          flattenBean(value, prefix ? `${prefix}.${name}` : name),
        )
        .reduce((c, n) => ({ ...c, ...n }), {});
    }
  }
}

const flattenConfigurationPropertiesBeans = (configprops) => {
  const propertySources = [];
  const contextNames = Object.keys(configprops.contexts);

  for (const contextName of contextNames) {
    const context = configprops.contexts[contextName];
    const beanNames = Object.keys(context.beans);

    for (const beanName of beanNames) {
      const bean = context.beans[beanName];
      const properties = mapKeys(
        flattenBean(bean.properties),
        (value, key) => `${bean.prefix}.${key}`,
      );
      propertySources.push({
        name:
          contextNames.length > 1 ? `${contextName}: ${beanName}` : beanName,
        properties,
      });
    }
  }

  return propertySources;
};

export default {
  components: { SbaInstanceSection },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    configprops: null,
    filter: null,
  }),
  computed: {
    configurationPropertiesBeans() {
      if (!this.configprops) {
        return [];
      }
      const configurationProperties = flattenConfigurationPropertiesBeans(
        this.configprops,
      );
      if (!this.filter) {
        return configurationProperties;
      }
      return configurationProperties
        .map(filterConfigurationProperties(this.filter.toLowerCase()))
        .filter((ps) => ps && Object.keys(ps.properties).length > 0);
    },
  },
  created() {
    this.fetchConfigprops();
  },
  methods: {
    async fetchConfigprops() {
      this.error = null;
      try {
        const configprops = await this.instance.fetchConfigprops();
        this.configprops = configprops.data;
      } catch (error) {
        console.warn('Fetching configuration properties failed:', error);
        this.error = error;
      }
      this.hasLoaded = true;
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/configprops',
      parent: 'instances',
      path: 'configprops',
      component: this,
      label: 'instances.configprops.label',
      group: VIEW_GROUP.INSIGHTS,
      order: 110,
      isEnabled: ({ instance }) => instance.hasEndpoint('configprops'),
    });
  },
};
</script>
