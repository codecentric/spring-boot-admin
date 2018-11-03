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
  <div>
    <details-datasource v-for="dataSource in dataSources" :key="dataSource"
                        :instance="instance" :data-source="dataSource"
    />
  </div>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import Instance from '@/services/instance';
  import {concatMap, timer} from '@/utils/rxjs';
  import detailsDatasource from './details-datasource';

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    mixins: [subscribing],
    // eslint-disable-next-line vue/no-unused-components
    components: {detailsDatasource},
    data: () => ({
      dataSources: [],
    }),
    methods: {
      async fetchDataSources() {
        const response = await this.instance.fetchMetric('data.source.active.connections');
        return response.data.availableTags.filter(tag => tag.tag === 'name')[0].values;
      },
      createSubscription() {
        const vm = this;
        return timer(0, 2500)
          .pipe(concatMap(this.fetchDataSources))
          .subscribe({
            next: names => {
              vm.dataSources = names
            },
            error: error => {
              console.warn('Fetching datasources failed:', error);
            }
          });
      }
    }
  }
</script>
