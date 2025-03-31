<!--
  - Copyright 2014-2020 the original author or authors.
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
  <table class="table-auto w-full">
    <thead>
      <tr>
        <th class="threads__thread-name" v-text="$t('term.name')" />
        <th class="threads__timeline">
          <svg class="threads__scale" height="24px" />
        </th>
      </tr>
    </thead>
    <template v-for="thread in threadTimelines" :key="thread.threadId">
      <tbody>
        <tr>
          <td class="threads__thread-name">
            <thread-tag :thread-state="thread.threadState" />
            <span v-text="thread.threadName" />
          </td>
          <td class="threads__timeline">
            <svg :id="`thread-${thread.threadId}`" height="32px" />
          </td>
        </tr>
        <tr
          v-if="showDetails[thread.threadId]"
          :key="`${thread.threadId}-detail`"
        >
          <td colspan="2">
            <thread-list-item
              :thread="thread"
              :details="getThreadDetails(thread, showDetails[thread.threadId])"
            />
          </td>
        </tr>
      </tbody>
    </template>
  </table>
</template>
<script>
import moment from 'moment';

import d3 from '@/utils/d3';
import ThreadListItem from '@/views/instances/threaddump/thread-list-item';
import threadTag from '@/views/instances/threaddump/thread-tag';

const maxPixelsPerSeconds = 15;

export default {
  components: { ThreadListItem, threadTag },
  props: {
    threadTimelines: {
      type: Object,
      required: true,
    },
  },
  data: () => ({
    showDetails: {},
    lastEndPosition: 0,
  }),
  watch: {
    threadTimelines: {
      deep: true,
      handler: 'drawTimelines',
      immediate: true,
    },
  },
  methods: {
    getThreadDetails(thread, start) {
      return thread.timeline.find((entry) => entry.start === start).details;
    },
    getTimeExtent(timelines) {
      return Object.entries(timelines)
        .map(([, value]) => value.timeline)
        .map((timeline) => ({
          start: timeline[0].start,
          end: timeline[timeline.length - 1].end,
        }))
        .reduce(
          (current, next) => ({
            start: Math.min(current.start, next.start),
            end: Math.max(current.end, next.end),
          }),
          { start: Number.MAX_SAFE_INTEGER, end: Number.MIN_SAFE_INTEGER },
        );
    },
    showThreadDetails({ threadId, start }) {
      const previousSelectedStart = this.showDetails[threadId];
      if (previousSelectedStart) {
        d3.selectAll(
          '#rect-threadid-' + threadId + '-start-' + previousSelectedStart,
        ).attr('class', (d) => `thread thread--${d.threadState.toLowerCase()}`);
      }

      if (previousSelectedStart === start) {
        this.showDetails = {
          ...this.showDetails,
          [threadId]: null,
        };
      } else {
        this.showDetails = {
          ...this.showDetails,
          [threadId]: start,
        };
        d3.selectAll('#rect-threadid-' + threadId + '-start-' + start).attr(
          'class',
          (d) =>
            `thread thread--${d.threadState.toLowerCase()} thread--clicked`,
        );
      }
    },
    async drawTimelines(timelines) {
      if (timelines) {
        const wasInView = this.isInView(this.lastEndPosition);
        await this.$nextTick();

        const { start, end } = this.getTimeExtent(timelines);
        const width = this.$el
          .querySelector('.threads__timeline')
          .getBoundingClientRect().width;
        const totalSeconds = Math.floor(width / maxPixelsPerSeconds);
        const x = d3
          .scaleTime()
          .range([0, width])
          .domain([start, Math.max(start + (totalSeconds + 1) * 1000, end)]);

        d3.select('.threads__scale')
          .attr('width', width)
          .call(
            d3
              .axisBottom(x)
              .ticks(Math.max(2, Math.floor(width / 50)))
              .tickFormat((d) => moment(d).format('HH:mm:ss')),
          );

        Object.entries(timelines).forEach(([threadId, value]) => {
          const svg = d3.select(`#thread-${threadId}`).attr('width', width);
          const d = svg.selectAll('rect').data(value.timeline);

          d.enter()
            .append('rect')
            .attr(
              'id',
              'rect-threadid-' +
                threadId +
                '-start-' +
                value.timeline[value.timeline.length - 1].start,
            )
            .attr(
              'class',
              (d) => `thread thread--${d.threadState.toLowerCase()}`,
            )
            .merge(d)
            .attr('height', '2em')
            .attr('x', (d) => x(d.start))
            .transition(150)
            .attr('width', (d) =>
              Math.max(x(d.end) - x(d.start), x(d.start + 500) - x(d.start)),
            );

          d3.selectAll(
            '#rect-threadid-' +
              threadId +
              '-start-' +
              value.timeline[value.timeline.length - 1].start,
          ).on('click', (event, d) =>
            this.showThreadDetails({ threadId: threadId, start: d.start }),
          );
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
      return (
        scrollable &&
        xPos >= scrollable.scrollLeft &&
        xPos <= scrollable.scrollLeft + scrollable.clientWidth
      );
    },
  },
};
</script>
<style lang="css">
.threads {
  table-layout: fixed;
}
.threads__thread-name {
  width: 250px;
  max-width: 750px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.threads__thread-name:hover {
  height: auto;
  overflow: visible;
  white-space: normal;
}

.threads__timeline {
  width: auto;
  overflow: hidden;
  text-overflow: ellipsis;
  padding-left: 0 !important;
  padding-right: 0 !important;
}
.threads__timeline svg {
  display: block;
}
.threads__scale .domain {
  display: none;
}
.thread {
  stroke: #000;
  stroke-width: 1px;
  stroke-opacity: 0.1;
}
.thread--runnable {
  fill: #48c78e;
}
.thread--runnable:hover,
.thread--runnable.thread--clicked {
  fill: #288159;
}
.thread--waiting {
  fill: #ffe08a;
}
.thread--waiting:hover,
.thread--waiting.thread--clicked {
  fill: #ffc524;
}
.thread--timed_waiting {
  fill: #ffe08a;
}
.thread--timed_waiting:hover,
.thread--timed_waiting.thread--clicked {
  fill: #ffc524;
}
.thread--blocked {
  fill: #f14668;
}
.thread--blocked:hover,
.thread--blocked.thread--clicked {
  fill: #ffc524;
}
</style>
