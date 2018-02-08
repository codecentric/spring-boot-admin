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
                        <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"></font-awesome-icon>
                        Fetching environment failed.
                    </strong>
                </div>
            </div>
            <div class="content" v-if="env && env.activeProfiles.length > 0">
                <div class="field is-grouped is-grouped-multiline">
                    <div class="control" v-for="profile in env.activeProfiles">
                        <div class="tags has-addons">
                            <span class="tag is-medium">Profile</span>
                            <span class="tag is-medium is-info" v-text="profile"></span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="content" v-if="env && hasEnvManagerSupport">
                <sba-env-manager :instance="instance" :property-sources="env.propertySources"
                                 @refresh="fetchEnv()" @update="fetchEnv" @reset="fetchEnv()">
                </sba-env-manager>
            </div>
            <div class="content" v-if="env">
                <div class="field has-addons">
                    <p class="control is-expanded">
                        <input class="input" type="search" placeholder="name / value filter" v-model="filter">
                    </p>
                </div>
            </div>
            <div class="content" v-if="env">
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
  import sbaEnvManager from './env-manager';

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
    components: {sbaEnvManager},
    data: () => ({
      hasLoaded: false,
      error: null,
      env: null,
      filter: null,
      hasEnvManagerSupport: false
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
      this.determineEnvManagerSupport();
    },
    watch: {
      instance() {
        this.fetchEnv();
        this.determineEnvManagerSupport();
      }
    },
    methods: {
      async fetchEnv() {
        if (this.instance) {
          this.error = null;
          try {
            const res = await this.instance.fetchEnv();
            this.env = res.data;
          } catch (error) {
            console.warn('Fetching environment failed:', error);
            this.error = error;
          }
          this.hasLoaded = true;
        }
      },
      async determineEnvManagerSupport() {
        if (this.instance) {
          try {
            this.hasEnvManagerSupport = await this.instance.hasEnvManagerSupport();
          } catch (error) {
            console.warn('Determine env manager support failed:', error);
            this.hasEnvManagerSupport = false;
          }
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