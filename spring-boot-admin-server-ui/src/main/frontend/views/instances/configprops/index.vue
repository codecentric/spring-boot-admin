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
  <section class="section" :class="{ 'is-loading' : !hasLoaded }">
    <div class="container">
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"/>
            Fetching configuration properties failed.
          </strong>
          <p v-text="error.message"/>
        </div>
      </div>
      <div class="field has-addons" v-if="configprops">
        <p class="control is-expanded">
          <input class="input" type="search" placeholder="name / value filter" v-model="filter">
        </p>
      </div>
      <sba-panel class="property-source" :header-sticks-below="['#navigation', '#instance-tabs']"
                 v-for="propertySource in propertySources" :key="propertySource.name"
                 :title="propertySource.name">
        <table class="table is-fullwidth"
               v-if="Object.keys(propertySource.properties).length > 0">
          <tr v-for="(value, name) in propertySource.properties" :key="`${propertySource-name}-${name}`">
            <td v-text="name"/>
            <td class="is-breakable" v-text="value"/>
          </tr>
        </table>
        <p class="is-muted" v-else>No properties set</p>
      </sba-panel>
    </div>
  </section>
</template>

<script>
  import Instance from '@/services/instance';
  import _ from 'lodash';

  const filterProperty = (needle) => (value, name) => {
    return name.toString().toLowerCase().includes(needle) || value.toString().toLowerCase().includes(needle);
  };
  const filterProperties = (needle, properties) => _.pickBy(properties, filterProperty(needle));
  const filterPropertySource = (needle) => (propertySource) => {
    if (!propertySource || !propertySource.properties) {
      return null;
    }
    return {
      ...propertySource,
      properties: filterProperties(needle, propertySource.properties)
    };
  };
  const flatten = (data) => {
    var result = {};
    function recurse(cur, prop) {
      if (Object(cur) !== cur) {
        result[prop] = cur;
      } else if (Array.isArray(cur)) {
        for (var i = 0, l = cur.length; i < l; i++) {
          recurse(cur[i], prop + "[" + i + "]");
        }
        if (l == 0) {
          result[prop] = [];
        }
      } else {
        var isEmpty = true;
        for (var p in cur) {
          isEmpty = false;
          recurse(cur[p], prop ? prop + "." + p : p);
        }
        if (isEmpty && prop) {
          result[prop] = {};
        }
      }
    }
    recurse(data, "");
    return result;
  }
  const createPropertySources = (configprops) => {
    var propertySources = [];
    var contextNames = Object.keys(configprops.contexts);
    for (var i = 0; i < contextNames.length; i += 1) {
      var contextName = contextNames[i];
      var context = configprops.contexts[contextName];
      var beanNames = Object.keys(context.beans);
      for (var j = 0; j < beanNames.length; j += 1) {
        var beanName = beanNames[j];
        var prefix = context.beans[beanName].prefix;
        var flattenedProperties = flatten(context.beans[beanName].properties);
        flattenedProperties = _.mapKeys(flattenedProperties, (value, key) => {
          return prefix + "." + key;
        });
        propertySources.push({
          name: contextName + "-" + beanName,
          properties: flattenedProperties
        });
      }
    }
    return propertySources;
  };
  
  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    data: () => ({
      hasLoaded: false,
      error: null,
      configprops: null,
      filter: null
    }),
    computed: {
      propertySources() {
        if (!this.configprops) {
          return [];
        }
        const propertySources = createPropertySources(this.configprops);
        if (!this.filter) {
          return propertySources;
        }
        return propertySources
          .map(filterPropertySource(this.filter.toLowerCase()))
          .filter(ps => ps && Object.keys(ps.properties).length > 0);
      }
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
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/configprops',
        parent: 'instances',
        path: 'configprops',
        component: this,
        label: 'Configuration Properties',
        order: 100,
        isEnabled: ({instance}) => instance.hasEndpoint('configprops')
      });
    }
  }
</script>
