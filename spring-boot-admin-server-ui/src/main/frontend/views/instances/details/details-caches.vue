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
    <details-cache
      v-for="cache in caches"
      :key="cache"
      :cache-name="cache"
      :instance="instance"
    />
  </div>
</template>

<script>
import { uniq } from 'lodash-es';
import { take } from 'rxjs/operators';

import subscribing from '@/mixins/subscribing';
import sbaConfig from '@/sba-config';
import Instance from '@/services/instance';
import { concatMap, delay, retryWhen, timer } from '@/utils/rxjs';
import detailsCache from '@/views/instances/details/details-cache';

export default {
  components: { detailsCache },
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    caches: [],
  }),
  methods: {
    async fetchCaches() {
      const response = await this.instance.fetchMetric('cache.gets');
      return uniq(
        response.data.availableTags.filter((tag) => tag.tag === 'name')[0]
          .values,
      );
    },
    createSubscription() {
      return timer(0, sbaConfig.uiSettings.pollTimer.cache)
        .pipe(
          concatMap(this.fetchCaches),
          retryWhen((err) => {
            return err.pipe(delay(1000), take(5));
          }),
        )
        .subscribe({
          next: (names) => {
            this.caches = names;
          },
          error: (error) => {
            console.warn('Fetching caches failed:', error);
          },
        });
    },
  },
};
</script>
