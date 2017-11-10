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
    <section class="section" :class="{ 'is-loading' : !env }">
        <div class="container" v-if="env">
            <div class="content" v-if="env.activeProfiles.length > 0">
                <div class="field is-grouped is-grouped-multiline">
                    <div class="control" v-for="profile in env.activeProfiles">
                        <div class="tags has-addons">
                            <span class="tag is-medium">Profile</span>
                            <span class="tag is-medium is-info" v-text="profile"></span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="content">
                <div class="field has-addons">
                    <p class="control is-expanded">
                        <input class="input" type="text" placeholder="name / value" v-model="filter">
                    </p>
                </div>
            </div>
            <div class="content">
                <sba-panel class="property-source"
                           v-for="propertySource in propertySources" :key="propertySource.name"
                           :title="propertySource.name">
                    <table class="table is-fullwidth" slot="text"
                           v-if="Object.keys(propertySource.properties).length > 0">
                        <tr v-for="(value, name) in propertySource.properties">
                            <td>
                                <span v-text="name"></span><br>
                                <small class="is-muted" v-if="value.origin" v-text="value.origin"></small>
                            </td>
                            <td class="is-breakable" v-text="value.value"></td>
                        </tr>
                    </table>
                    <p class="is-muted" v-else slot="text">No properties set</p>
                </sba-panel>
            </div>
        </div>
    </section>
</template>

<script>
  import _ from 'lodash';

  const filterProperty = (needle) => (property, name) => {
    return name.toString().toLowerCase().indexOf(needle) >= 0 || property.value.toString().toLowerCase().indexOf(needle) >= 0;
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

  export default {
    props: ['instance'],

    data: () => ({
      filter: null,
      env: null
    }),
    computed: {
      propertySources() {
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
    },
    watch: {
      instance(newVal, oldVal) {
        if (newVal !== oldVal) {
          this.fetchEnv();
        }
      }
    },
    methods: {
      async fetchEnv() {
        if (this.instance) {
          const res = await this.instance.fetchEnv();
          this.env = res.data;
        }
      }
    }
  }
</script>

<style lang="scss">
    @import "~@/assets/css/utilities";

    .property-source .card-header {
        position: sticky;
        background: $white;
        top: ($navbar-height-px + $tabs-height-px);
    }
</style>