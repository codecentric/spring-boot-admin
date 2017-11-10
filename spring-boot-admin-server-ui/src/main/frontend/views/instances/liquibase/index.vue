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
    <section class="section" :class="{ 'is-loading' : !reports }">
        <div class="container" v-if="reports">
            <div class="content">
                <template v-for="(report, name) in reports">
                    <h4 class="title" v-text="name" :key="name"></h4>
                    <sba-panel v-for="changeSet in report.changeSets" :key="`${name}-${changeSet.id}`"
                               :title="`${changeSet.id}: ${changeSet.description}`" class="change-set">
                        <change-set :change-set="changeSet" slot="text"></change-set>
                    </sba-panel>
                </template>
            </div>
        </div>
    </section>
</template>

<script>
  import changeSet from './change-set'

  export default {
    props: ['instance'],
    components: {
      changeSet
    },
    data: () => ({
      reports: null
    }),
    computed: {},
    created() {
      this.fetchLiquibase();
    },
    watch: {
      instance(newVal, oldVal) {
        if (newVal !== oldVal) {
          this.fetchLiquibase();
        }
      }
    },
    methods: {
      async fetchLiquibase() {
        if (this.instance) {
          const res = await this.instance.fetchLiquibase();
          this.reports = res.data;
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