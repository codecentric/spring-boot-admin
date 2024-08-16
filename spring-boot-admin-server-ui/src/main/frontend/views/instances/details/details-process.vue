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
  <sba-panel v-if="hasLoaded" :title="$t('instances.details.process.title')">
    <div>
      <sba-alert v-if="error" :error="error" :title="$t('term.fetch_failed')" />
      <div v-else class="-mx-4 -my-3">
        <sba-key-value-table :map="tableData">
          <template #uptime="value">
            <process-uptime :value="value.value" />
          </template>
        </sba-key-value-table>
      </div>
    </div>
  </sba-panel>
</template>

<script>
import { take } from 'rxjs/operators';

import subscribing from '@/mixins/subscribing';
import sbaConfig from '@/sba-config';
import Instance from '@/services/instance';
import { concatMap, delay, retryWhen, timer } from '@/utils/rxjs';
import processUptime from '@/views/instances/details/process-uptime';
import { toMillis } from '@/views/instances/metrics/metric';

export default {
  components: { processUptime },
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    pid: null,
    uptime: { value: null, baseUnit: null },
    systemCpuLoad: null,
    processCpuLoad: null,
    systemCpuCount: null,
  }),
  computed: {
    tableData() {
      return {
        pid: {
          label: this.$t('instances.details.process.pid'),
          value: this.pid,
        },
        uptime: {
          label: this.$t('instances.details.process.uptime'),
          value: toMillis(this.uptime.value, this.uptime.baseUnit),
        },
        processCpuLoad: {
          label: this.$t('instances.details.process.process_cpu_usage'),
          value: this.processCpuLoad?.toFixed(2),
        },
        systemCpuLoad: {
          label: this.$t('instances.details.process.system_cpu_usage'),
          value: this.systemCpuLoad?.toFixed(2),
        },
        cpus: {
          label: this.$t('instances.details.process.cpus'),
          value: this.systemCpuCount,
        },
      };
    },
  },
  created() {
    this.fetchPid();
    this.fetchUptime();
    this.fetchCpuCount();
  },
  methods: {
    toMillis,
    async fetchUptime() {
      try {
        const response = await this.fetchMetric('process.uptime');
        this.uptime = {
          value: response.measurements[0].value,
          baseUnit: response.baseUnit,
        };
      } catch (error) {
        this.error = error;
        console.warn('Fetching Uptime failed:', error);
      } finally {
        this.hasLoaded = true;
      }
    },
    async fetchPid() {
      if (this.instance.hasEndpoint('env')) {
        try {
          const response = await this.instance.fetchEnv('PID');
          this.pid = response.data.property.value;
        } catch (error) {
          console.warn('Fetching PID failed:', error);
        } finally {
          this.hasLoaded = true;
        }
      }
    },
    async fetchCpuCount() {
      try {
        this.systemCpuCount = (
          await this.fetchMetric('system.cpu.count')
        ).measurements[0].value;
      } catch (error) {
        console.warn('Fetching Cpu Count failed:', error);
      } finally {
        this.hasLoaded = true;
      }
    },
    createSubscription() {
      return timer(0, sbaConfig.uiSettings.pollTimer.process)
        .pipe(
          concatMap(this.fetchCpuLoadMetrics),
          retryWhen((err) => {
            return err.pipe(delay(1000), take(5));
          }),
        )
        .subscribe({
          next: (data) => {
            this.processCpuLoad = data.processCpuLoad;
            this.systemCpuLoad = data.systemCpuLoad;
          },
          error: (error) => {
            this.hasLoaded = true;
            console.warn('Fetching CPU Usage metrics failed:', error);
            this.error = error;
          },
        });
    },
    async fetchCpuLoadMetrics() {
      const fetchProcessCpuLoad = this.fetchMetric('process.cpu.usage');
      const fetchSystemCpuLoad = this.fetchMetric('system.cpu.usage');
      let processCpuLoad;
      let systemCpuLoad;
      try {
        processCpuLoad = (await fetchProcessCpuLoad).measurements[0].value;
      } catch (error) {
        console.warn('Fetching Process CPU Load failed:', error);
      }
      try {
        systemCpuLoad = (await fetchSystemCpuLoad).measurements[0].value;
      } catch (error) {
        console.warn('Fetching Sytem CPU Load failed:', error);
      }
      return {
        processCpuLoad,
        systemCpuLoad,
      };
    },
    async fetchMetric(name) {
      const response = await this.instance.fetchMetric(name);
      return response.data;
    },
  },
};
</script>
