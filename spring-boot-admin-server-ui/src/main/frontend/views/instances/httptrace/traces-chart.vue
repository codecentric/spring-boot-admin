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
  <div class="trace-chart">
    <div
      v-if="tooltipSelection"
      :class="`trace-chart__tooltip--${
        x(tooltipSelection[0]) > width / 2 ? 'left' : 'right'
      }`"
      class="trace-chart__tooltip"
    >
      <table class="is-narrow is-size-7">
        <tr>
          <th v-text="$t('instances.httptrace.chart.total_requests')" />
          <td v-text="tooltipContent.totalCount" />
        </tr>
        <tr>
          <th v-text="$t('instances.httptrace.chart.successful_requests')" />
          <td v-text="tooltipContent.totalSuccess" />
        </tr>
        <tr>
          <th v-text="$t('instances.httptrace.chart.status_4xx')" />
          <td v-text="tooltipContent.totalClientErrors" />
        </tr>
        <tr>
          <th v-text="$t('instances.httptrace.chart.status_5xx')" />
          <td v-text="tooltipContent.totalServerErrors" />
        </tr>
        <tr>
          <th v-text="$t('instances.httptrace.chart.max_time')" />
          <td v-text="`${tooltipContent.maxTime}ms`" />
        </tr>
        <tr>
          <th v-text="$t('instances.httptrace.chart.avg_time')" />
          <td v-text="`${tooltipContent.avgTime}ms`" />
        </tr>
      </table>
    </div>
    <svg class="trace-chart__svg" />
  </div>
</template>

<script>
//see https://github.com/d3/d3/issues/2733#issuecomment-190743489
import moment from 'moment';

import d3 from '@/utils/d3';

const interval = 1000;
export default {
  props: {
    traces: {
      type: Array,
      default: () => [],
    },
  },
  emits: ['selected'],
  data: () => ({
    brushSelection: null,
    hovered: null,
  }),
  computed: {
    chartData() {
      if (this.traces.length <= 0) {
        return [];
      }
      const chartData = [];
      const now = moment().valueOf();
      let idx = this.traces.length - 1;
      const oldest = this.traces[this.traces.length - 1].timestamp.valueOf();

      for (
        let time = Math.floor(oldest.valueOf() / interval) * interval;
        time < now;
        time += interval
      ) {
        const bucket = {
          timeStart: time,
          timeEnd: time + interval,
          totalCount: 0,
          totalSuccess: 0,
          totalClientErrors: 0,
          totalServerErrors: 0,
          totalTime: 0,
          maxTime: 0,
        };

        while (
          idx >= 0 &&
          this.traces[idx].timestamp.valueOf() < time + interval
        ) {
          const trace = this.traces[idx];
          bucket.totalCount++;
          if (trace.isSuccess()) {
            bucket.totalSuccess++;
          } else if (trace.isClientError()) {
            bucket.totalClientErrors++;
          } else if (trace.isServerError()) {
            bucket.totalServerErrors++;
          }
          if (trace.timeTaken) {
            bucket.totalTime += trace.timeTaken;
            bucket.maxTime = Math.max(bucket.maxTime, trace.timeTaken);
          }
          idx--;
        }
        chartData.push(bucket);
      }

      return chartData;
    },
    tooltipSelection() {
      return this.brushSelection
        ? this.brushSelection
        : this.hovered
          ? [this.hovered, this.hovered + interval]
          : null;
    },
    tooltipContent() {
      const selection = this.tooltipSelection;
      const totals = this.chartData
        .filter(
          (bucket) =>
            bucket.timeStart.valueOf() >= selection[0] &&
            bucket.timeStart.valueOf() < selection[1],
        )
        .reduce(
          (current, next) => ({
            totalCount: current.totalCount + next.totalCount,
            totalSuccess: current.totalSuccess + next.totalSuccess,
            totalClientErrors:
              current.totalClientErrors + next.totalClientErrors,
            totalServerErrors:
              current.totalServerErrors + next.totalServerErrors,
            totalTime: current.totalTime + next.totalTime,
            maxTime: Math.max(current.maxTime, next.maxTime),
          }),
          {
            totalCount: 0,
            totalSuccess: 0,
            totalClientErrors: 0,
            totalServerErrors: 0,
            totalTime: 0,
            maxTime: 0,
          },
        );
      return {
        ...totals,
        avgTime:
          totals.totalCount > 0
            ? Math.floor(totals.totalTime / totals.totalCount)
            : 0,
      };
    },
  },
  watch: {
    chartData: 'drawChart',
    hovered(newVal) {
      if (newVal) {
        this.hover
          .attr('opacity', 1)
          .attr('d', `M${this.x(newVal)},${this.height} ${this.x(newVal)},0`);
      } else {
        this.hover.attr('opacity', 0);
      }
    },
    brushSelection(newVal) {
      this.$emit('selected', newVal);
    },
  },
  mounted() {
    const margin = {
      top: 20,
      right: 20,
      bottom: 30,
      left: 20,
    };

    this.width =
      this.$el.getBoundingClientRect().width - margin.left - margin.right;
    this.height =
      this.$el.getBoundingClientRect().height - margin.top - margin.bottom;

    this.chartLayer = d3
      .select(this.$el.querySelector('.trace-chart__svg'))
      .append('g')
      .attr('transform', `translate(${margin.left},${margin.top})`);

    this.xAxis = this.chartLayer
      .append('g')
      .attr('class', 'trace-chart__axis-x')
      .attr('transform', `translate(0,${this.height})`);

    this.yAxis = this.chartLayer
      .append('g')
      .attr('class', 'trace-chart__axis-y')
      .attr('stroke', null);

    this.areas = this.chartLayer.append('g');

    this.hover = this.chartLayer
      .append('path')
      .attr('class', 'trace-chart__hover')
      .attr('opacity', 0);

    this.brushGroup = this.chartLayer
      .append('g')
      .attr('class', 'trace-chart__brush');

    this.drawChart(this.chartData);
  },
  methods: {
    drawChart(data) {
      ///setup x and y scale
      const x = d3
        .scaleTime()
        .range([0, this.width])
        .domain(d3.extent(data, (d) => d.timeStart));
      this.x = x;

      const y = d3
        .scaleLinear()
        .range([this.height, 0])
        .domain([0, d3.max(data, (d) => d.totalCount)]);

      //draw areas
      const area = d3
        .area()
        .x((d) => x(d.data.timeStart))
        .y0((d) => y(d[0]))
        .y1((d) => y(d[1]));

      const stack = d3
        .stack()
        .keys(['totalSuccess', 'totalClientErrors', 'totalServerErrors']);

      const d = this.areas.selectAll('.trace-chart__area').data(stack(data));

      d.enter()
        .append('path')
        .merge(d)
        .attr('class', (d) => `trace-chart__area trace-chart__area--${d.key}`)
        .attr('d', area);

      d.exit().remove();

      //draw axis
      this.xAxis.call(
        d3
          .axisBottom(x)
          .ticks(10)
          .tickFormat((d) => moment(d).format('HH:mm:ss')),
      );

      this.yAxis
        .call(
          d3
            .axisRight(y)
            .ticks(
              Math.min(
                5,
                d3.max(data, (d) => d.totalCount),
              ),
            )
            .tickSize(this.width),
        )
        .call((axis) =>
          axis
            .selectAll('.tick text')
            .attr('x', -2)
            .attr('dy', 2)
            .attr('text-anchor', 'end'),
        );

      //draw brush selection
      const brush = d3
        .brushX()
        .extent([
          [0, 0],
          [this.width, this.height],
        ])
        .on('start', (event) => {
          if (event.selection) {
            this.isBrushing = true;
            this.hovered = null;
          }
        })
        .on('brush', (event) => {
          if (!event.sourceEvent) {
            return;
          }

          if (event.selection) {
            const floor =
              Math.floor(x.invert(event.selection[0]) / interval) * interval;
            const ceil =
              Math.ceil(x.invert(event.selection[1]) / interval) * interval;
            d3.select(this).call(event.target.move, [floor, ceil].map(x));
            this.brushSelection = [floor, ceil];
          }
        })
        .on('end', (event) => {
          this.isBrushing = false;
          if (!event.selection) {
            this.brushSelection = null;
          }
        });

      this.brushGroup
        .call(brush)
        .on('mousemove', (event) => {
          if (this.isBrushing) {
            return;
          }
          const mouseX = d3.pointer(
            event,
            this.brushGroup.select('.overlay').node(),
          )[0];
          this.hovered = Math.floor(x.invert(mouseX) / interval) * interval;
        })
        .on('mouseout', () => {
          this.hovered = null;
        });

      brush.move(
        this.brushGroup,
        this.brushSelection ? this.brushSelection.map(x) : null,
      );
    },
  },
};
</script>

<style lang="css">
.trace-chart__svg {
  height: 200px;
  width: 100%;
}
.trace-chart__hover {
  stroke: #b5b5b5;
  stroke-width: 1px;
}
.trace-chart__tooltip {
  position: absolute;
  background: #000;
  opacity: 0.8;
  pointer-events: none;
  border-radius: 6px;
  padding: 0.825em;
  width: 200px;
}
.trace-chart__tooltip table th,
.trace-chart__tooltip table td {
  border: none;
  color: #b5b5b5;
  padding: 0.25em 0.75em;
}
.trace-chart__tooltip table td {
  text-align: right;
}
.trace-chart__tooltip--left {
  left: 5px;
}
.trace-chart__tooltip--right {
  right: 5px;
}
.trace-chart .selection {
  stroke: none;
  fill: rgba(0, 0, 0, 0.2);
  fill-opacity: 1;
}
.trace-chart__axis-y .domain {
  stroke: none;
}
.trace-chart__axis-y .tick:not(:first-of-type) line {
  stroke-dasharray: 2, 2;
  stroke: #b5b5b5;
}
.trace-chart__area--totalSuccess {
  fill: #48c78e;
  opacity: 0.8;
}
.trace-chart__area--totalClientErrors {
  fill: #ffe08a;
  opacity: 0.8;
}
.trace-chart__area--totalServerErrors {
  fill: #f14668;
  opacity: 0.8;
}
</style>
