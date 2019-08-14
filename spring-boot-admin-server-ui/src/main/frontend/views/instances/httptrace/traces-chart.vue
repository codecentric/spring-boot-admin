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
  <div class="trace-chart">
    <div class="trace-chart__tooltip"
         v-if="tooltipSelection "
         :class="`trace-chart__tooltip--${this.x(tooltipSelection[0]) > this.width / 2 ? 'left' : 'right'}`"
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
  import d3 from '@/utils/d3';
  import {event as d3Event} from 'd3-selection';
  import moment from 'moment';

  const interval = 1000;
  export default {
    props: {
      traces: {
        type: Array,
        default: () => []
      }
    },
    data: () => ({
      brushSelection: null,
      hovered: null
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

        for (let time = Math.floor(oldest.valueOf() / interval) * interval; time < now; time += interval) {
          const bucket = {
            timeStart: time,
            timeEnd: time + interval,
            totalCount: 0,
            totalSuccess: 0,
            totalClientErrors: 0,
            totalServerErrors: 0,
            totalTime: 0,
            maxTime: 0
          };

          while (idx >= 0 && this.traces[idx].timestamp.valueOf() < time + interval) {
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
        return this.brushSelection ? this.brushSelection : this.hovered ? [this.hovered, this.hovered + interval] : null;
      },
      tooltipContent() {
        const selection = this.tooltipSelection;
        const totals = this.chartData.filter(
          bucket => bucket.timeStart.valueOf() >= selection[0] && bucket.timeStart.valueOf() < selection[1]
        ).reduce(
          (current, next) => ({
            totalCount: current.totalCount + next.totalCount,
            totalSuccess: current.totalSuccess + next.totalSuccess,
            totalClientErrors: current.totalClientErrors + next.totalClientErrors,
            totalServerErrors: current.totalServerErrors + next.totalServerErrors,
            totalTime: current.totalTime + next.totalTime,
            maxTime: Math.max(current.maxTime, next.maxTime)
          }), {
            totalCount: 0,
            totalSuccess: 0,
            totalClientErrors: 0,
            totalServerErrors: 0,
            totalTime: 0,
            maxTime: 0
          });
        return {
          ...totals,
          avgTime: totals.totalCount > 0 ? Math.floor(totals.totalTime / totals.totalCount) : 0
        };
      }
    },
    methods: {
      drawChart(data) {
        const vm = this;

        ///setup x and y scale
        const x = d3.scaleTime()
          .range([0, vm.width])
          .domain(d3.extent(data, d => d.timeStart));
        this.x = x;

        const y = d3.scaleLinear()
          .range([vm.height, 0])
          .domain([0, d3.max(data, d => d.totalCount)]);

        //draw areas
        const area = d3.area()
          .x(d => x(d.data.timeStart))
          .y0(d => y(d[0]))
          .y1(d => y(d[1]));

        const stack = d3.stack()
          .keys(['totalSuccess', 'totalClientErrors', 'totalServerErrors']);

        const d = vm.areas.selectAll('.trace-chart__area')
          .data(stack(data));

        d.enter().append('path')
          .merge(d)
          .attr('class', d => `trace-chart__area trace-chart__area--${d.key}`)
          .attr('d', area);

        d.exit().remove();

        //draw axis
        vm.xAxis.call(d3.axisBottom(x)
          .ticks(10)
          .tickFormat(d => moment(d).format('HH:mm:ss'))
        );

        vm.yAxis.call(d3.axisRight(y)
          .ticks(Math.min(5, d3.max(data, d => d.totalCount)))
          .tickSize(this.width)
        ).call(
          axis => axis.selectAll('.tick text')
            .attr('x', -2)
            .attr('dy', 2)
            .attr('text-anchor', 'end')
        );

        //draw brush selection
        const brush = d3.brushX()
          .extent([[0, 0], [vm.width, vm.height]])
          .on('start', () => {
            if (d3Event.selection) {
              vm.isBrushing = true;
              vm.hovered = null;
            }
          })
          .on('brush', function () {
            if (d3Event.sourceEvent === null || d3Event.sourceEvent.type === 'brush') {
              return;
            }

            if (d3Event.selection) {
              const floor = Math.floor(x.invert(d3Event.selection[0]) / interval) * interval;
              const ceil = Math.ceil(x.invert(d3Event.selection[1]) / interval) * interval;
              d3.select(this).call(d3Event.target.move, [floor, ceil].map(x));
              vm.brushSelection = [floor, ceil];
            }
          })
          .on('end', () => {
            vm.isBrushing = false;
            if (!d3Event.selection) {
              vm.brushSelection = null;
            }
          });

        vm.brushGroup.call(brush)
          .on('mousemove', () => {
            if (vm.isBrushing) {
              return;
            }
            const mouseX = d3.mouse(vm.brushGroup.select('.overlay').node())[0];
            vm.hovered = Math.floor(x.invert(mouseX) / interval) * interval;
          }).on('mouseout', () => {
          vm.hovered = null;
        });

        brush.move(vm.brushGroup, vm.brushSelection ? vm.brushSelection.map(x) : null);
      },
    },
    mounted() {
      const margin = {
        top: 20,
        right: 20,
        bottom: 30,
        left: 20,
      };

      this.width = this.$el.getBoundingClientRect().width - margin.left - margin.right;
      this.height = this.$el.getBoundingClientRect().height - margin.top - margin.bottom;

      this.chartLayer = d3.select(this.$el.querySelector('.trace-chart__svg'))
        .append('g')
        .attr('transform', `translate(${margin.left},${margin.top})`);

      this.xAxis = this.chartLayer.append('g')
        .attr('class', 'trace-chart__axis-x')
        .attr('transform', `translate(0,${this.height})`);

      this.yAxis = this.chartLayer.append('g')
        .attr('class', 'trace-chart__axis-y')
        .attr('stroke', null);

      this.areas = this.chartLayer.append('g');

      this.hover = this.chartLayer.append('path')
        .attr('class', 'trace-chart__hover')
        .attr('opacity', 0);

      this.brushGroup = this.chartLayer.append('g')
        .attr('class', 'trace-chart__brush');

      this.drawChart(this.chartData);
    },
    watch: {
      chartData: 'drawChart',
      hovered(newVal) {
        if (newVal) {
          this.hover.attr('opacity', 1)
            .attr('d', `M${this.x(newVal)},${this.height} ${this.x(newVal)},0`);
        } else {
          this.hover.attr('opacity', 0);
        }
      },
      brushSelection(newVal) {
        this.$emit('selected', newVal);
      }
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .trace-chart {
    &__svg {
      height: 200px;
      width: 100%;
    }

    &__hover {
      stroke: $grey-light;
      stroke-width: 1px;
    }

    &__tooltip {
      position: absolute;
      background: $black;
      opacity: 0.8;
      pointer-events: none;
      border-radius: $radius-large;
      padding: 0.825em;
      width: 200px;

      & table th,
      & table td {
        border: none;
        color: $grey-light;
        padding: 0.25em 0.75em;
      }

      & table td {
        text-align: right;
      }

      &--left {
        left: 5px;
      }
      &--right {
        right: 5px;
      }
    }

    & .selection {
      stroke: none;
      fill: rgba(0, 0, 0, 0.2);
      fill-opacity: 1;
    }

    &__axis-y {
      & .domain {
        stroke: none;
      }

      .tick:not(:first-of-type) {
        & line {
          stroke-dasharray: 2, 2;
          stroke: $grey-light;
        }
      }
    }

    &__area {
      &--totalSuccess {
        fill: $success;
        opacity: 0.8;
      }

      &--totalClientErrors {
        fill: $warning;
        opacity: 0.8;
      }

      &--totalServerErrors {
        fill: $danger;
        opacity: 0.8;
      }
    }
  }
</style>
