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
  <sba-instance-section :error="errorFetch" :loading="!hasLoaded">
    <template v-if="threads" #before>
      <sba-sticky-subnav>
        <div class="text-right">
          <sba-button @click="downloadThreaddump">
            <font-awesome-icon icon="download" />&nbsp;
            <span v-text="$t('instances.threaddump.download')" />
          </sba-button>
        </div>
      </sba-sticky-subnav>
    </template>
    <sba-alert
      v-if="errorDownload"
      :error="errorDownload"
      :title="$t('instances.threaddump.download_failed')"
    />
    <sba-panel>
      <threads-list v-if="threads" :thread-timelines="threads" />
    </sba-panel>
  </sba-instance-section>
</template>

<script>
import { remove } from 'lodash-es';
import moment from 'moment';
import { take } from 'rxjs/operators';

import subscribing from '@/mixins/subscribing';
import Instance from '@/services/instance';
import { concatMap, delay, retryWhen, timer } from '@/utils/rxjs';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';
import threadsList from '@/views/instances/threaddump/threads-list';

export default {
  components: { SbaInstanceSection, threadsList },
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    errorFetch: null,
    errorDownload: null,
    threads: {},
  }),
  computed: {},
  methods: {
    updateTimelines(threads) {
      const now = moment().valueOf();
      //initialize with all known live threads, which will be removed from the list if still alive
      const terminatedThreads = Object.entries(this.threads)
        .filter(([, value]) => value.threadState !== 'TERMINATED')
        .map(([threadId]) => parseInt(threadId));

      threads.forEach((thread) => {
        if (!this.threads[thread.threadId]) {
          this.threads[thread.threadId] = {
            threadId: thread.threadId,
            threadState: thread.threadState,
            threadName: thread.threadName,
            timeline: [
              {
                start: now,
                end: now,
                details: thread,
                threadState: thread.threadState,
              },
            ],
          };
        } else {
          const entry = this.threads[thread.threadId];
          if (entry.threadState !== thread.threadState) {
            entry.threadState = thread.threadState;
            entry.timeline[entry.timeline.length - 1].end = now;
            entry.timeline.push({
              start: now,
              end: now,
              details: thread,
              threadState: thread.threadState,
            });
          } else {
            entry.timeline[entry.timeline.length - 1].end = now;
          }
        }
        remove(terminatedThreads, (threadId) => threadId === thread.threadId);
      });

      terminatedThreads.forEach((threadId) => {
        const entry = this.threads[threadId];
        entry.threadState = 'TERMINATED';
        entry.timeline[entry.timeline.length - 1].end = now;
      });
    },
    async fetchThreaddump() {
      const response = await this.instance.fetchThreaddump();
      return response.data.threads;
    },
    async downloadThreaddump() {
      this.errorDownload = null;
      try {
        await this.instance.downloadThreaddump();
      } catch (error) {
        console.warn('Downloading thread dump failed.', error);
        this.errorDownload = error;
      }
    },
    createSubscription() {
      this.errorFetch = null;
      return timer(0, 1000)
        .pipe(
          concatMap(this.fetchThreaddump),
          retryWhen((err) => {
            return err.pipe(delay(1000), take(2));
          }),
        )
        .subscribe({
          next: (threads) => {
            this.hasLoaded = true;
            this.updateTimelines(threads);
          },
          error: (error) => {
            this.hasLoaded = true;
            console.warn('Fetching threaddump failed:', error);
            this.errorFetch = error;
          },
        });
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/threaddump',
      parent: 'instances',
      path: 'threaddump',
      label: 'instances.threaddump.label',
      component: this,
      group: VIEW_GROUP.JVM,
      order: 400,
      isEnabled: ({ instance }) => instance.hasEndpoint('threaddump'),
    });
  },
};
</script>
