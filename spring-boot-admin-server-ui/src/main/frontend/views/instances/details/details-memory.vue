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
  <sba-panel :title="$t('instances.details.memory.title') + `: ${name}`" v-if="hasLoaded">
    <div>
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
            <span v-text="$t('instances.details.memory.fetch_failed')" />
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <div class="level memory-current" v-if="current">
        <div class="level-item has-text-centered" v-if="current.metaspace">
          <div>
            <p class="heading has-bullet has-bullet-primary" v-text="$t('instances.details.memory.metaspace')" />
            <p v-text="prettyBytes(current.metaspace)" />
          </div>
        </div>
        <div class="level-item has-text-centered">
          <div>
            <p class="heading has-bullet has-bullet-info" v-text="$t('instances.details.memory.used')" />
            <p v-text="prettyBytes(current.used)" />
          </div>
        </div>
        <div class="level-item has-text-centered">
          <div>
            <p class="heading has-bullet has-bullet-warning" v-text="$t('instances.details.memory.size')" />
            <p v-text="prettyBytes(current.committed)" />
          </div>
        </div>
        <div class="level-item has-text-centered" v-if="current.max >= 0">
          <div>
            <p class="heading" v-text="$t('instances.details.memory.max')" />
            <p v-text="prettyBytes(current.max)" />
          </div>
        </div>
      </div>
      <mem-chart v-if="chartData.length > 0" :data="chartData" />
    </div>
  </sba-panel>
</template>

<script>
  import sbaConfig from '@/sba-config'
  import subscribing from '@/mixins/subscribing';
  import Instance from '@/services/instance';
  import {concatMap, timer} from '@/utils/rxjs';
  import moment from 'moment';
  import prettyBytes from 'pretty-bytes';
  import memChart from './mem-chart';

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      },
      type: {
        type: String,
        required: true
      }
    },
    mixins: [subscribing],
    components: {memChart},
    data: () => ({
      hasLoaded: false,
      error: null,
      current: null,
      chartData: []
    }),
    computed: {
      name() {
        switch (this.type) {
          case 'heap':
            return 'Heap';
          case 'nonheap':
            return 'Non heap';
          default:
            return this.type;
        }
      }
    },
    methods: {
      prettyBytes,
      async fetchMetrics() {
        const responseMax = this.instance.fetchMetric('jvm.memory.max', {area: this.type});
        const responseUsed = this.instance.fetchMetric('jvm.memory.used', {area: this.type});
        const hasMetaspace = (await responseUsed).data.availableTags.some(tag => tag.tag === 'id' && tag.values.includes('Metaspace'));
        const responeMetaspace = this.type === 'nonheap' && hasMetaspace
          ? this.instance.fetchMetric('jvm.memory.used', {area: this.type, id: 'Metaspace'})
          : null;
        const responseCommitted = this.instance.fetchMetric('jvm.memory.committed', {area: this.type});
        return {
          max: (await responseMax).data.measurements[0].value,
          used: (await responseUsed).data.measurements[0].value,
          metaspace: responeMetaspace ? (await responeMetaspace).data.measurements[0].value : null,
          committed: (await responseCommitted).data.measurements[0].value
        };
      },
      createSubscription() {
        const vm = this;
        return timer(0, sbaConfig.uiSettings.pollTimer.memory)
          .pipe(concatMap(this.fetchMetrics))
          .subscribe({
            next: data => {
              vm.hasLoaded = true;
              vm.current = data;
              vm.chartData.push({...data, timestamp: moment().valueOf()});
            },
            error: error => {
              vm.hasLoaded = true;
              console.warn('Fetching memory metrics failed:', error);
              vm.error = error;
            }
          });
      }
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .memory-current {
    margin-bottom: 0 !important;
  }
</style>
