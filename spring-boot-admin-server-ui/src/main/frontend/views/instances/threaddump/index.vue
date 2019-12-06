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
    <template v-if="hasLoaded">
      <div v-if="errorFetch" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
            <span v-text="$t('instances.threaddump.fetch_failed')" />
          </strong>
          <p v-text="errorFetch.message" />
        </div>
      </div>
      <div v-if="errorDownload" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
            <span v-text="$t('instances.threaddump.download_failed')" />
          </strong>
          <p v-text="errorDownload.message" />
        </div>
      </div>
      <div v-if="threads" class="control">
        <button class="button is-primary" @click="downloadThreaddump">
          <font-awesome-icon icon="download" />&nbsp;
          <span v-text="$t('instances.threaddump.download')" />
        </button>
      </div>
      <threads-list v-if="threads" :thread-timelines="threads" />
    </template>
  </section>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import Instance from '@/services/instance';
  import {concatMap, timer} from '@/utils/rxjs';
  import remove from 'lodash/remove';
  import moment from 'moment-shortformat';
  import threadsList from './threads-list';
  import {VIEW_GROUP} from '../../index';

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    mixins: [subscribing],
    components: {threadsList},
    data: () => ({
      hasLoaded: false,
      errorFetch: null,
      errorDownload: null,
      threads: null
    }),
    computed: {},
    methods: {
      updateTimelines(threads) {
        const vm = this;
        const now = moment().valueOf();
        vm.threads = vm.threads || {};
        //initialize with all known live threads, which will be removed from the list if still alive
        const terminatedThreads = Object.entries(vm.threads)
          .filter(([, value]) => value.threadState !== 'TERMINATED')
          .map(([threadId]) => parseInt(threadId));

        threads.forEach(
          thread => {
            if (!vm.threads[thread.threadId]) {
              vm.$set(vm.threads, thread.threadId, {
                threadId: thread.threadId,
                threadState: thread.threadState,
                threadName: thread.threadName,
                timeline: [{
                  start: now,
                  end: now,
                  details: thread,
                  threadState: thread.threadState,
                }]
              });
            } else {
              const entry = vm.threads[thread.threadId];
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
            remove(terminatedThreads, threadId => threadId === thread.threadId);
          });

        terminatedThreads.forEach(threadId => {
          const entry = vm.threads[threadId];
          entry.threadState = 'TERMINATED';
          entry.timeline[entry.timeline.length - 1].end = now;
        });
      },
      async fetchThreaddump() {
        const response = await this.instance.fetchThreaddump();
        return response.data.threads;
      },
      async downloadThreaddump() {
        const vm = this;
        vm.errorDownload = null;
        try {
          await this.instance.downloadThreaddump();
        } catch(error) {
          console.warn('Downloading thread dump failed.', error);
          vm.errorDownload = error;
        }
      },
      createSubscription() {
        const vm = this;
        vm.errorFetch = null;
        return timer(0, 1000)
          .pipe(concatMap(vm.fetchThreaddump))
          .subscribe({
            next: threads => {
              vm.hasLoaded = true;
              vm.updateTimelines(threads);
            },
            error: error => {
              vm.hasLoaded = true;
              console.warn('Fetching threaddump failed:', error);
              vm.errorFetch = error;
            }
          });
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/threaddump',
        parent: 'instances',
        path: 'threaddump',
        label: 'instances.threaddump.label',
        component: this,
        group: VIEW_GROUP.JVM,
        order: 400,
        isEnabled: ({instance}) => instance.hasEndpoint('threaddump')
      });
    }
  }
</script>
