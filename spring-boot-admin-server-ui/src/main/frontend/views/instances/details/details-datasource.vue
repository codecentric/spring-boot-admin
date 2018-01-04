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
    <sba-panel title="DataSources" v-if="hasDataSources">
        <div class="content" slot="text">
            <table class="table data-table">
                <template v-for="dataSource in dataSources">
                    <tr>
                        <td :rowspan="3" v-text="dataSource.name"></td>
                        <td colspan="2">
                            <span class="is-pulled-left">active connections</span>
                            <span v-text="dataSource.active"></span>
                            <progress v-if="dataSource.max >= 0"
                                      class="progress is-small" :value="dataSource.active"
                                      :max="dataSource.max"></progress>
                        </td>
                    </tr>
                    <tr>
                        <td>min connections</td>
                        <td class="has-text-right" v-text="dataSource.min"></td>
                    </tr>
                    <tr>
                        <td>max connections</td>
                        <td class="has-text-right" v-text="dataSource.max" v-if="dataSource.max >= 0"></td>
                        <td class="has-text-right" v-else>unlimited</td>
                    </tr>
                </template>
            </table>
        </div>
    </sba-panel>
</template>

<script>
  export default {
    props: ['instance'],
    data: () => ({
      hasDataSources: false,
      dataSources: [],
    }),
    created() {
      this.fetchMetrics();
    },
    watch: {
      instance(newVal, oldVal) {
        if (newVal !== oldVal) {
          this.fetchMetrics();
        }
      }
    },
    methods: {
      async fetchMetrics() {
        if (this.instance) {
          try {
            const response = await this.instance.fetchMetric('data.source.active.connections');
            this.hasDataSources = true;
            const dataSourcesNames = response.data.availableTags.filter(tag => tag.tag === 'name')[0].values;
            dataSourcesNames.forEach(this.fetchDataSourceMetrics);
          } catch (error) {
            this.hasDataSources = false;
          }
        }
      },
      async fetchDataSourceMetrics(name) {
        const responseActive = this.instance.fetchMetric('data.source.active.connections', {name});
        const responseMin = this.instance.fetchMetric('data.source.min.connections', {name});
        const responseMax = this.instance.fetchMetric('data.source.max.connections', {name});

        this.dataSources.push({
          name,
          active: (await responseActive).data.measurements[0].value,
          min: (await responseMin).data.measurements[0].value,
          max: (await responseMax).data.measurements[0].value
        });
      }
    }
  }
</script>