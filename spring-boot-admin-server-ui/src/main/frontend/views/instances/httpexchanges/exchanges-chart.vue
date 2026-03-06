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
  <div class="exchange-chart">
    <canvas ref="chartCanvas" />
  </div>
</template>

<script>
import {
  BarController,
  BarElement,
  CategoryScale,
  Chart,
  LinearScale,
  TimeScale,
  Tooltip,
} from 'chart.js';
import 'chartjs-adapter-moment';
import { parse } from 'iso8601-duration';
import { markRaw } from 'vue';

import { toMilliseconds } from '@/utils/iso8601-duration';

Chart.register(
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  TimeScale,
  Tooltip,
);

const interval = 1000;

export default {
  props: {
    exchanges: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    chart: null,
    cachedChartData: [],
  }),
  computed: {
    chartData() {
      if (this.exchanges.length <= 0) {
        return [];
      }
      const chartData = [];
      let idx = this.exchanges.length - 1;
      const oldest =
        this.exchanges[this.exchanges.length - 1].timestamp.valueOf();
      const newest = this.exchanges[0].timestamp.valueOf();

      for (
        let time = Math.floor(oldest.valueOf() / interval) * interval;
        time <= newest;
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
          this.exchanges[idx].timestamp.valueOf() < time + interval
        ) {
          const exchange = this.exchanges[idx];
          bucket.totalCount++;
          if (exchange.isSuccess()) {
            bucket.totalSuccess++;
          } else if (exchange.isClientError()) {
            bucket.totalClientErrors++;
          } else if (exchange.isServerError()) {
            bucket.totalServerErrors++;
          }
          if (exchange.timeTaken) {
            let timeTakenInMillis = toMilliseconds(parse(exchange.timeTaken));
            bucket.totalTime += timeTakenInMillis;
            bucket.maxTime = Math.max(bucket.maxTime, timeTakenInMillis);
          }
          idx--;
        }
        chartData.push(bucket);
      }

      return chartData;
    },
  },
  watch: {
    chartData: {
      handler(newData) {
        this.cachedChartData = JSON.parse(JSON.stringify(newData));
        this.updateChart();
      },
      deep: true,
    },
  },
  mounted() {
    this.cachedChartData = JSON.parse(JSON.stringify(this.chartData));
    this.initChart();
  },
  beforeUnmount() {
    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }
  },
  methods: {
    initChart() {
      const ctx = this.$refs.chartCanvas.getContext('2d');
      const chartDataRef = this.cachedChartData;

      this.chart = markRaw(
        new Chart(ctx, {
          type: 'bar',
          data: {
            labels: [],
            datasets: [
              {
                label: this.$t(
                  'instances.httpexchanges.chart.successful_requests',
                ),
                data: [],
                backgroundColor: 'rgba(72, 199, 142, 0.8)',
                stack: 'stack0',
              },
              {
                label: this.$t('instances.httpexchanges.chart.status_4xx'),
                data: [],
                backgroundColor: 'rgba(255, 224, 138, 0.8)',
                stack: 'stack0',
              },
              {
                label: this.$t('instances.httpexchanges.chart.status_5xx'),
                data: [],
                backgroundColor: 'rgba(241, 70, 104, 0.8)',
                stack: 'stack0',
              },
            ],
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
              x: {
                type: 'time',
                time: {
                  unit: 'second',
                  displayFormats: {
                    second: 'HH:mm:ss',
                  },
                },
                grid: {
                  display: false,
                },
              },
              y: {
                stacked: true,
                beginAtZero: true,
                grid: {
                  color: 'rgba(181, 181, 181, 0.2)',
                  drawBorder: false,
                },
                ticks: {
                  precision: 0,
                },
              },
            },
            plugins: {
              tooltip: {
                mode: 'index',
                intersect: false,
                callbacks: {
                  footer: (tooltipItems) => {
                    const index = tooltipItems[0].dataIndex;
                    const bucket = chartDataRef[index];
                    if (!bucket) return '';

                    const avgTime =
                      bucket.totalCount > 0
                        ? Math.floor(bucket.totalTime / bucket.totalCount)
                        : 0;

                    return [
                      `${this.$t('instances.httpexchanges.chart.total_requests')}: ${bucket.totalCount}`,
                      `${this.$t('instances.httpexchanges.chart.max_time')}: ${bucket.maxTime.toFixed(0)}ms`,
                      `${this.$t('instances.httpexchanges.chart.avg_time')}: ${avgTime.toFixed(0)}ms`,
                    ];
                  },
                },
              },
            },
          },
        }),
      );

      this.updateChart();
    },
    updateChart() {
      if (!this.chart || !this.cachedChartData) return;

      const data = this.cachedChartData;
      const labels = data.map((d) => d.timeStart);
      const successData = data.map((d) => d.totalSuccess);
      const clientErrorData = data.map((d) => d.totalClientErrors);
      const serverErrorData = data.map((d) => d.totalServerErrors);

      this.chart.data.labels = labels;
      this.chart.data.datasets[0].data = successData;
      this.chart.data.datasets[1].data = clientErrorData;
      this.chart.data.datasets[2].data = serverErrorData;

      this.chart.update('none');
    },
  },
};
</script>

<style lang="css">
.exchange-chart {
  height: 200px;
  width: 100%;
}

.exchange-chart canvas {
  max-height: 200px;
}
</style>
