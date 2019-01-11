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
  <div class="mem-chart">
    <svg class="mem-chart__svg" />
  </div>
</template>

<script>
  import d3 from '@/utils/d3';
  import moment from 'moment';
  import prettyBytes from 'pretty-bytes';

  export default {
    props: {
      data: {
        type: Array,
        default: () => []
      }
    },
    data: () => ({}),
    methods: {
      drawChart(_data) {
        const vm = this;
        const data = _data.length === 1 ? _data.concat([{..._data[0], timestamp: _data[0].timestamp + 1}]) : _data;

        ///setup x and y scale
        const extent = d3.extent(data, d => d.timestamp);
        const x = d3.scaleTime()
          .range([0, vm.width])
          .domain(extent);

        const y = d3.scaleLinear()
          .range([vm.height, 0])
          .domain([0, d3.max(data, d => d.committed) * 1.05]);

        //draw max
        const max = vm.areas.selectAll('.mem-chart__line--max')
          .data([data]);
        max.enter().append('path')
          .merge(max)
          .attr('class', 'mem-chart__line--max')
          .attr('d', d3.line()
            .x(d => x(d.timestamp))
            .y(d => y(d.max)));
        max.exit().remove();

        //draw areas
        const committed = vm.areas.selectAll('.mem-chart__area--committed')
          .data([data]);
        committed.enter().append('path')
          .merge(committed)
          .attr('class', 'mem-chart__area--committed')
          .attr('d', d3.area()
            .x(d => x(d.timestamp))
            .y0(d => y(d.used))
            .y1(d => y(d.committed)));
        committed.exit().remove();

        const used = vm.areas.selectAll('.mem-chart__area--used')
          .data([data]);
        used.enter().append('path')
          .merge(used)
          .attr('class', 'mem-chart__area--used')
          .attr('d', d3.area()
            .x(d => x(d.timestamp))
            .y0(d => y(d.metaspace || 0))
            .y1(d => y(d.used)));
        used.exit().remove();

        const metaspace = vm.areas.selectAll('.mem-chart__area--metaspace')
          .data([data]);
        metaspace.enter().append('path')
          .merge(metaspace)
          .attr('class', 'mem-chart__area--metaspace')
          .attr('d', d3.area()
            .x(d => x(d.timestamp))
            .y0(y(0))
            .y1(d => y(d.metaspace)));
        metaspace.exit().remove();

        //draw axis
        vm.xAxis.call(d3.axisBottom(x)
          .ticks(5)
          .tickFormat(d => moment(d).format('HH:mm:ss'))
        );

        vm.yAxis.call(d3.axisLeft(y)
          .ticks(5)
          .tickFormat(prettyBytes)
        );

      },
    },
    mounted() {
      const margin = {
        top: 5,
        right: 5,
        bottom: 30,
        left: 50,
      };

      this.width = this.$el.getBoundingClientRect().width - margin.left - margin.right;
      this.height = this.$el.getBoundingClientRect().height - margin.top - margin.bottom;

      this.chartLayer = d3.select(this.$el.querySelector('.mem-chart__svg'))
        .append('g')
        .attr('transform', `translate(${margin.left},${margin.top})`);

      this.xAxis = this.chartLayer.append('g')
        .attr('class', 'mem-chart__axis-x')
        .attr('transform', `translate(0,${this.height})`);

      this.yAxis = this.chartLayer.append('g')
        .attr('class', 'mem-chart__axis-y')
        .attr('stroke', null);

      this.areas = this.chartLayer.append('g');

      this.drawChart(this.data);
    },
    watch: {
      data: 'drawChart'
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .mem-chart {
    &__svg {
      height: 159px;
      width: 100%;
    }

    &__area {
      &--committed {
        fill: $warning;
        opacity: 0.8;
      }
      &--used {
        fill: $info;
        opacity: 0.8;
      }
      &--metaspace {
        fill: $primary;
        opacity: 0.8;
      }
    }

    &__line--max {
      stroke: $grey;
    }
  }
</style>
