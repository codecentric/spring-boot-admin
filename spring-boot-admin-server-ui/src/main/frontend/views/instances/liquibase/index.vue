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
    <div class="container" v-if="hasLoaded">
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"/>
            Fetching Liquibase migrations failed.
          </strong>
          <p v-text="error.message"/>
        </div>
      </div>
      <template v-for="(report, name) in reports">
        <h4 class="title" v-text="name" :key="name"/>
        <sba-panel v-for="changeSet in report.changeSets" :key="`${name}-${changeSet.id}`"
                   :title="`${changeSet.id}: ${changeSet.description}`" class="change-set">
          <change-set :change-set="changeSet"/>
        </sba-panel>
      </template>
    </div>
  </section>
</template>

<script>
  import Instance from '@/services/instance';
  import changeSet from './change-set';

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    components: {
      changeSet
    },
    data: () => ({
      hasLoaded: false,
      error: null,
      reports: []
    }),
    computed: {},
    created() {
      this.fetchLiquibase();
    },
    methods: {
      async fetchLiquibase() {
        if (this.instance) {
          this.error = null;
          try {
            const res = await this.instance.fetchLiquibase();
            this.reports = res.data;
          } catch (error) {
            console.warn('Fetching Liquibase migrations failed:', error);
            this.error = error;
          }
          this.hasLoaded = true;
        }
      }
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .change-set .card-header {
    position: sticky;
    background: $white;
    top: ($navbar-height-px + $tabs-height-px);
  }
</style>
