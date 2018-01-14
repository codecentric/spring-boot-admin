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
    <section class="section" :class="{ 'is-loading' : !threads }">
        <div class="container" v-if="threads">
            <div class="content">
                <threads-list :thread-timelines="threads"></threads-list>
            </div>
        </div>
    </section>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import {Observable} from '@/utils/rxjs';
  import _ from 'lodash';
  import moment from 'moment-shortformat';
  import threadsList from './threads-list';

  export default {
    props: ['instance'],
    mixins: [subscribing],
    components: {
      threadsList
    },
    data: () => ({
      threads: null
    }),
    computed: {},
    watch: {
      instance() {
        this.subscribe()
      },
    },
    methods: {
      updateTimelines(threads) {
        const vm = this;
        const now = moment.now().valueOf();
        vm.threads = vm.threads || {};
        //initialize with all known live threads, which will be removed from the list if still alive
        const terminatedThreads = _.entries(vm.threads).filter(([threadId, value]) => value.threadState !== 'TERMINATED')
          .map(([threadId]) => parseInt(threadId));

        threads.forEach(
          thread => {
            if (!vm.threads[thread.threadId]) {
              vm.$set(vm.threads, thread.threadId, {
                threadId: thread.threadId,
                threadState: thread.threadState,
                threadName: thread.threadName,
                details: thread,
                timeline: [{
                  start: now,
                  end: now,
                  threadState: thread.threadState,
                }]
              });
            } else {
              const entry = vm.threads[thread.threadId];
              entry.details = thread;
              if (entry.threadState !== thread.threadState) {
                entry.threadState = thread.threadState;
                entry.timeline.push({
                  start: entry.timeline[entry.timeline.length - 1].end,
                  end: now,
                  threadState: thread.threadState,
                });
              } else {
                entry.timeline[entry.timeline.length - 1].end = now;
              }
            }
            _.remove(terminatedThreads, threadId => threadId === thread.threadId);
          });

        terminatedThreads.forEach(threadId => {
          const entry = vm.threads[threadId];
          entry.threadState = 'TERMINATED';
          entry.details = null;
          entry.timeline[entry.timeline.length - 1].end = now;
        });
      },
      async fetchThreaddump() {
        const response = await this.instance.fetchThreaddump();
        return response.data.threads;
      },
      createSubscription() {
        const vm = this;
        if (this.instance) {
          return Observable.timer(0, 1000)
            .concatMap(vm.fetchThreaddump)
            .subscribe({
              next: threads => {
                vm.updateTimelines(threads);
              },
              errors: err => {
                vm.unsubscribe();
              }
            });
        }
      }
    }
  }
</script>