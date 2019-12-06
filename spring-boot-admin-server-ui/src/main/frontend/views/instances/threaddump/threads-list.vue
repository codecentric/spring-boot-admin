<!--
  - Copyright 2014-2019 the original author or authors.
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
  <table class="threads table is-fullwidth is-hoverable">
    <thead>
      <tr>
        <th class="threads__thread-name" v-text="$t('term.name')" />
        <th class="threads__timeline">
          <svg class="threads__scale" height="24px" />
        </th>
      </tr>
    </thead>
    <tbody>
      <template v-for="thread in threadTimelines">
        <tr class="is-selectable" :key="thread.threadId">
          <td class="threads__thread-name">
            <thread-tag :thread-state="thread.threadState" />
            <span v-text="thread.threadName" />
          </td>
          <td class="threads__timeline">
            <svg :id="`thread-${thread.threadId}`" height="32px" />
          </td>
        </tr>
        <tr :key="`${thread.threadId}-detail`"
            v-if="showDetails[thread.threadId]"
        >
          <td colspan="2">
            <table class="threads__thread-details table is-narrow is-fullwidth has-background-white-ter">
              <tr>
                <td v-text="$t('instances.threaddump.thread_id')" />
                <td v-text="thread.threadId" />
              </tr>
              <tr>
                <td v-text="$t('instances.threaddump.thread_name')" />
                <td v-text="thread.threadName" />
              </tr>
              <template v-if="getThreadDetails(thread) !== null">
                <tr>
                  <td v-text="$t('instances.threaddump.thread_state')" />
                  <td v-text="getThreadDetails(thread).threadState" />
                </tr>
                <tr>
                  <td v-text="$t('instances.threaddump.thread_details_blocked_count')" />
                  <td v-text="getThreadDetails(thread).blockedCount" />
                </tr>
                <tr>
                  <td v-text="$t('instances.threaddump.thread_details_blocked_time')" />
                  <td v-text="getThreadDetails(thread).blockedTime" />
                </tr>
                <tr>
                  <td v-text="$t('instances.threaddump.thread_details_waited_count')" />
                  <td v-text="getThreadDetails(thread).waitedCount" />
                </tr>
                <tr>
                  <td v-text="$t('instances.threaddump.thread_details_waited_time')" />
                  <td v-text="getThreadDetails(thread).waitedTime" />
                </tr>
                <tr>
                  <td v-text="$t('instances.threaddump.thread_details_lock_name')" />
                  <td v-text="getThreadDetails(thread).lockName" />
                </tr>
                <tr>
                  <td v-text="$t('instances.threaddump.thread_details_lock_owner_id')" />
                  <td v-text="getThreadDetails(thread).lockOwnerId" />
                </tr>
                <tr>
                  <td v-text="$t('instances.threaddump.thread_details_lock_owner_name')" />
                  <td v-text="getThreadDetails(thread).lockOwnerName" />
                </tr>
                <tr v-if="getThreadDetails(thread).stackTrace.length > 0">
                  <td colspan="2">
                    <span v-text="$t('term.stacktrace')" />
                    <pre class="threads__thread-stacktrace"><template
                      v-for="(frame, idx) in getThreadDetails(thread).stackTrace"
                    ><span
                      :key="`frame-${thread.threadId}-${idx}`"
                      v-text="`${frame.className}.${frame.methodName}(${frame.fileName}:${frame.lineNumber})`"
                    /> <span
                      :key="`frame-${thread.threadId}-${idx}-native`"
                      class="tag is-dark" v-if="frame.nativeMethod"
                    >native</span>
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
    import moment from 'moment';
    import threadTag from './thread-tag';

    const maxPixelsPerSeconds = 15;

  export default {
    props: {
      threadTimelines: {
        type: Object,
        required: true
      }
    },
    data: () => ({
      showDetails: {},
      lastEndPosition: 0
    }),
    components: {threadTag},
    watch: {
      threadTimelines: {
        deep: true,
        handler: 'drawTimelines',
        immediate: true
      }
    },
    methods: {
      getThreadDetails(thread) {
        return thread.timeline.find(entry => entry.start === this.showDetails[thread.threadId]).details
      },
      getTimeExtent(timelines) {
        return Object.entries(timelines).map(([, value]) => value.timeline)
          .map(timeline => ({
            start: timeline[0].start,
            end: timeline[timeline.length - 1].end
          }))
          .reduce((current, next) => ({
            start: Math.min(current.start, next.start),
            end: Math.max(current.end, next.end)
          }), {start: Number.MAX_SAFE_INTEGER, end: Number.MIN_SAFE_INTEGER});
      },
      showThreadDetails({threadId, start}) {
        const previousSelectedStart = this.showDetails[threadId];
        if (previousSelectedStart) {
          d3.selectAll('#rect-threadid-' + threadId + '-start-' + previousSelectedStart)
            .attr('class', d => `thread thread--${d.threadState.toLowerCase()}`);
        }

        if (previousSelectedStart === start) {
          this.$delete(this.showDetails, threadId)
        } else {
          this.$set(this.showDetails, threadId, start);
          d3.selectAll('#rect-threadid-' + threadId + '-start-' + start)
            .attr('class', d => `thread thread--${d.threadState.toLowerCase()} thread--clicked`)
        }
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
              .tickFormat(d => moment(d).format('HH:mm:ss'))
            );

          Object.entries(timelines).forEach(([threadId, value]) => {
            const svg = d3.select(`#thread-${threadId}`).attr('width', width);
            const d = svg.selectAll('rect').data(value.timeline);

            d.enter()
              .append('rect')
              .attr('id', 'rect-threadid-' + threadId + '-start-' + value.timeline[value.timeline.length - 1].start)
              .attr('class', d => `thread thread--${d.threadState.toLowerCase()}`)
              .merge(d)
              .attr('height', '2em')
              .attr('x', d => x(d.start))
              .transition(150)
              .attr('width', d => Math.max(x(d.end) - x(d.start), x(d.start + 500) - x(d.start)));

            d3.selectAll('#rect-threadid-' + threadId + '-start-' + value.timeline[value.timeline.length - 1].start)
              .on('click', d => this.showThreadDetails({threadId: threadId, start: d.start}))
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
        return scrollable && xPos >= scrollable.scrollLeft && xPos <= (scrollable.scrollLeft + scrollable.clientWidth);
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

    &__thread-details {
      table-layout: fixed;

      & td:first-child:not(.threads__thread-stacktrace) {
        width: 20%;
      }
    }
    &__thread-stacktrace {
      overflow: auto;
      max-height: 300px;
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
    stroke: $black;
    stroke-width: 1px;
    stroke-opacity: .1;

    &--runnable {
      fill: $success;

      &:hover,
      &.thread--clicked {
        fill: darken($success, 20%)
      }
    }

    &--waiting {
      fill: $warning;

      &:hover,
      &.thread--clicked {
        fill: darken($warning, 20%)
      }
    }

    &--timed_waiting {
      fill: $warning;

      &:hover,
      &.thread--clicked {
        fill: darken($warning, 20%)
      }
    }

    &--blocked {
      fill: $danger;

      &:hover,
      &.thread--clicked {
        fill: darken($warning, 20%)
      }
    }
  }
</style>
