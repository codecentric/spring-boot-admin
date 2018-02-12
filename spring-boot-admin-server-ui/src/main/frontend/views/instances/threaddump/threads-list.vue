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
    <table class="threads table is-fullwidth">
        <thead>
        <tr>
            <th class="threads__thread-name">Name</th>
            <th class="threads__timeline">
                <svg class="threads__scale" height="24px"></svg>
            </th>
        </tr>
        </thead>
        <tbody>
        <template v-for="thread in threadTimelines">
            <tr class="is-selectable" :key="thread.threadId"
                @click="showDetails[thread.threadId] ? $delete(showDetails, thread.threadId) : $set(showDetails, thread.threadId, true)">
                <td class="threads__thread-name">
                    <thread-tag :thread-state="thread.threadState"></thread-tag>
                    <span v-text="thread.threadName"></span>
                </td>
                <td class="threads__timeline">
                    <svg :id="`thread-${thread.threadId}`" height="26px"></svg>
                </td>
            </tr>
            <tr :key="`${thread.threadId}-detail`"
                v-if="showDetails[thread.threadId]">
                <td colspan="2">
                    <table class="table is-narrow">
                        <tr>
                            <td>Thread Id</td>
                            <td v-text="thread.threadId"></td>
                        </tr>
                        <tr>
                            <td>Thread name</td>
                            <td v-text="thread.threadName"></td>
                        </tr>
                        <tr>
                            <td>Thread state</td>
                            <td v-text="thread.threadState"></td>
                        </tr>
                        <template v-if="thread.details !== null">
                            <tr>
                                <td>Blocked count</td>
                                <td v-text="thread.details.blockedCount"></td>
                            </tr>
                            <tr>
                                <td>Blocked time</td>
                                <td v-text="thread.details.blockedTime"></td>
                            </tr>
                            <tr>
                                <td>Waited count</td>
                                <td v-text="thread.details.waitedCount"></td>
                            </tr>
                            <tr>
                                <td>Waited time</td>
                                <td v-text="thread.details.waitedTime"></td>
                            </tr>
                            <tr>
                                <td>Lock name</td>
                                <td v-text="thread.details.lockName"></td>
                            </tr>
                            <tr>
                                <td>Lock owner id</td>
                                <td v-text="thread.details.lockOwnerId"></td>
                            </tr>
                            <tr>
                                <td>Lock owner name</td>
                                <td v-text="thread.details.lockOwnerName"></td>
                            </tr>
                            <tr v-if="thread.details.stackTrace.length > 0">
                                <td colspan="2">Stacktrace
                                    <pre><template
                                            v-for="(frame, idx) in thread.details.stackTrace"><span
                                            :key="`frame-${thread.threadId}-${idx}`"
                                            v-text="`${frame.className}.${frame.methodName}(${frame.fileName}:${frame.lineNumber})`"></span> <span
                                            :key="`frame-${thread.threadId}-${idx}-native`"
                                            class="tag is-dark" v-if="frame.nativeMethod">native</span>
</template></pre>
                                </td>
                            </tr>
                        </template>
                    </table>

                </td>
            </tr>
        </template>
        </tbody>
    </table>
</template>
<script>
  import d3 from '@/utils/d3';
  import _ from 'lodash';
  import moment from 'moment';
  import threadTag from './thread-tag';

  const maxPixelsPerSeconds = 15;

  export default {
    props: ['threadTimelines'],
    data: () => ({
      showDetails: {},
      lastEndPosition: 0
    }),
    components: {
      threadTag
    },
    mounted() {
      this.drawTimelines(this.threadTimelines);
    },
    watch: {
      threadTimelines: {
        handler(newVal) {
          this.drawTimelines(newVal);
        },
        deep: true
      }
    },
    methods: {
      getTimeExtent(timelines) {
        return _.entries(timelines).map(([threadId, value]) => value.timeline)
          .map(timeline => ({
            start: timeline[0].start,
            end: timeline[timeline.length - 1].end
          }))
          .reduce((current, next) => ({
            start: Math.min(current.start, next.start),
            end: Math.max(current.end, next.end)
          }), {start: Number.MAX_SAFE_INTEGER, end: Number.MIN_SAFE_INTEGER});
      },
      async drawTimelines(timelines) {
        if (timelines) {
          const wasInView = this.isInView(this.lastEndPosition);
          await this.$nextTick();

          const {start, end} = this.getTimeExtent(timelines);
          const width = this.$el.querySelector('.threads__timeline').getBoundingClientRect().width;
          const totalSeconds = Math.floor(width / maxPixelsPerSeconds);
          const x = d3.scaleTime()
            .range([0, width])
            .domain([start, Math.max(start + (totalSeconds + 1) * 1000, end)]);

          d3.select('.threads__scale')
            .attr('width', width)
            .call(d3.axisBottom(x)
              .ticks(Math.max(2, Math.floor(width / 50)))
              .tickFormat(d => moment(d).format("HH:mm:ss"))
            );

          _.entries(timelines).forEach(([threadId, value]) => {
            const svg = d3.select(`#thread-${threadId}`).attr('width', width);
            const d = svg.selectAll('rect').data(value.timeline);

            d.enter()
              .append('rect')
              .merge(d)
              .attr('class', d => `thread--${d.threadState.toLowerCase()}`)
              .attr('height', '2em')
              .attr('x', d => x(d.start))
              .transition(150)
              .attr('width', d => Math.max(x(d.end) - x(d.start), x(d.start + 500) - x(d.start)))
          });

          this.lastEndPosition = x(end);
          if (wasInView && !this.isInView(this.lastEndPosition)) {
            const scrollable = this.$el;
            scrollable.scroll(this.lastEndPosition, scrollable.scrollHeight);
          }
        }
      },
      isInView(xPos) {
        const scrollable = this.$el;
        return xPos >= scrollable.scrollLeft && xPos <= scrollable.scrollLeft + scrollable.clientWidth;
      }
    }
  }
</script>
<style lang="scss">
    @import "~@/assets/css/utilities";

    .threads {
        table-layout: fixed;

        &__thread-name {
            width: 250px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        &__timeline {
            width: auto;
            overflow: hidden;
            text-overflow: ellipsis;
            padding-left: 0 !important;
            padding-right: 0 !important;

            & svg {
                display: block; //prevent margin bottom on svg
            }
        }

        &__scale {
            & .domain {
                display: none;
            }
        }
    }

    .thread {
        &--runnable {
            fill: $success;
        }

        &--timed_waiting,
        &--waiting {
            fill: $warning;
        }

        &--blocked {
            fill: $danger;
        }
    }
</style>