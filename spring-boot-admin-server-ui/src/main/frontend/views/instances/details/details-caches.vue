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
    <details-cache v-for="cache in caches" :key="cache"
                   :instance="instance" :cache-name="cache"
    />
  </div>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import Instance from '@/services/instance';
  import {concatMap, timer} from '@/utils/rxjs';
  import uniq from 'lodash/uniq';
  import detailsCache from './details-cache';

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    mixins: [subscribing],
    components: {detailsCache},
    data: () => ({
      caches: [],
    }),
    methods: {
      async fetchCaches() {
        const response = await this.instance.fetchMetric('cache.gets');
        return uniq(response.data.availableTags.filter(tag => tag.tag === 'name')[0].values);
      },
      createSubscription() {
        const vm = this;
        return timer(0, 2500)
          .pipe(concatMap(this.fetchCaches))
          .subscribe({
            next: names => {
              vm.caches = names
            },
            error: error => {
              console.warn('Fetching caches failed:', error);
            }
          });
      }
    }
  }
</script>
