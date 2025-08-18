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
  <sba-instance-section :error="err" :loading="!hasLoaded">
    <div v-if="!hasData" class="message is-warning">
      <div
        class="message-body"
        v-text="$t('instances.scheduledtasks.no_scheduledtasks')"
      />
    </div>

    <div class="flex flex-col">
      <template v-if="hasCronData">
        <sba-panel :title="$t('instances.scheduledtasks.cron.title')" seamless>
          <table class="table w-full table-striped">
            <thead>
              <tr>
                <th v-text="$t('instances.scheduledtasks.cron.runnable')" />
                <th v-text="$t('instances.scheduledtasks.cron.expression')" />
                <th v-text="$t('instances.scheduledtasks.next_execution')" />
                <th v-text="$t('instances.scheduledtasks.last_execution')" />
                <th
                  v-text="$t('instances.scheduledtasks.last_execution_status')"
                />
              </tr>
            </thead>
            <tbody v-for="task in cron" :key="task.runnable.target">
              <tr>
                <td
                  class="scheduledtasks__target"
                  v-text="task.runnable.target"
                />
                <td
                  class="font-mono text-sm"
                  :title="task.expression"
                  v-text="formatCron(task.expression)"
                />
                <scheduled-task-executions :task="task" />
              </tr>
            </tbody>
          </table>
        </sba-panel>
      </template>

      <template v-if="hasFixedDelayData">
        <sba-panel
          :title="$t('instances.scheduledtasks.fixed_delay.title')"
          seamless
        >
          <table class="table w-full table-striped">
            <thead>
              <tr>
                <th
                  v-text="$t('instances.scheduledtasks.fixed_delay.runnable')"
                />
                <th
                  v-text="
                    $t('instances.scheduledtasks.fixed_delay.initial_delay_ms')
                  "
                />
                <th
                  v-text="
                    $t('instances.scheduledtasks.fixed_delay.interval_ms')
                  "
                />
                <th v-text="$t('instances.scheduledtasks.next_execution')" />
                <th v-text="$t('instances.scheduledtasks.last_execution')" />
                <th
                  v-text="$t('instances.scheduledtasks.last_execution_status')"
                />
              </tr>
            </thead>
            <tbody v-for="task in fixedDelay" :key="task.runnable.target">
              <tr>
                <td v-text="task.runnable.target" />
                <td
                  :title="`${task.initialDelay}ms`"
                  v-text="formatTime(task.initialDelay)"
                />
                <td
                  :title="`${task.interval}ms`"
                  v-text="formatTime(task.interval)"
                />
                <scheduled-task-executions :task="task" />
              </tr>
            </tbody>
          </table>
        </sba-panel>
      </template>

      <template v-if="hasFixedRateData">
        <sba-panel
          :title="$t('instances.scheduledtasks.fixed_rate.title')"
          seamless
        >
          <table class="table w-full table-striped">
            <thead>
              <tr>
                <th
                  v-text="$t('instances.scheduledtasks.fixed_delay.runnable')"
                />
                <th
                  v-text="
                    $t('instances.scheduledtasks.fixed_delay.initial_delay_ms')
                  "
                />
                <th
                  v-text="
                    $t('instances.scheduledtasks.fixed_delay.interval_ms')
                  "
                />
                <th v-text="$t('instances.scheduledtasks.next_execution')" />
                <th v-text="$t('instances.scheduledtasks.last_execution')" />
                <th
                  v-text="$t('instances.scheduledtasks.last_execution_status')"
                />
              </tr>
            </thead>
            <tbody v-for="task in fixedRate" :key="task.runnable.target">
              <tr>
                <td v-text="task.runnable.target" />
                <td
                  :title="`${task.initialDelay}ms`"
                  v-text="formatTime(task.initialDelay)"
                />
                <td
                  :title="`${task.interval}ms`"
                  v-text="formatTime(task.interval)"
                />
                <scheduled-task-executions :task="task" />
              </tr>
            </tbody>
          </table>
        </sba-panel>
      </template>
    </div>
  </sba-instance-section>
</template>

<script>
import cronstrue from 'cronstrue/i18n';
import { useI18n } from 'vue-i18n';

import SbaPanel from '@/components/sba-panel.vue';

import Instance from '@/services/instance';
import { usePrettyTime } from '@/utils/prettyTime';
import { VIEW_GROUP } from '@/views/ViewGroup';
import ScheduledTaskExecutions from '@/views/instances/scheduledtasks/scheduled-task-executions.vue';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

export default {
  components: {
    ScheduledTaskExecutions,
    SbaPanel,
    SbaInstanceSection,
  },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  setup() {
    const { formatTime } = usePrettyTime();
    const { locale } = useI18n();

    return {
      formatTime,
      formatCron: (cron) =>
        cronstrue.toString(cron, {
          verbose: true,
          locale: locale.value,
          throwErrorOnParse: false,
        }),
    };
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    cron: null,
    fixedDelay: null,
    fixedRate: null,
  }),
  computed: {
    hasCronData: function () {
      return this.cron && this.cron.length;
    },
    hasFixedDelayData: function () {
      return this.fixedDelay && this.fixedDelay.length;
    },
    hasFixedRateData: function () {
      return this.fixedRate && this.fixedRate.length;
    },
    hasData: function () {
      return (
        this.hasCronData || this.hasFixedDelayData || this.hasFixedRateData
      );
    },
  },
  created() {
    this.fetchScheduledTasks();
  },
  methods: {
    async fetchScheduledTasks() {
      this.error = null;
      try {
        const res = await this.instance.fetchScheduledTasks();
        this.cron = res.data.cron;
        this.fixedDelay = res.data.fixedDelay;
        this.fixedRate = res.data.fixedRate;
      } catch (error) {
        console.warn('Fetching scheduled tasks failed:', error);
        this.error = error;
      }
      this.hasLoaded = true;
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/scheduledtasks',
      parent: 'instances',
      path: 'scheduledtasks',
      component: this,
      label: 'instances.scheduledtasks.label',
      group: VIEW_GROUP.INSIGHTS,
      order: 950,
      isEnabled: ({ instance }) => instance.hasEndpoint('scheduledtasks'),
    });
  },
};
</script>
<style lang="css">
.scheduledtasks__target {
  width: 250px;
  max-width: 750px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.scheduledtasks__target:hover {
  height: auto;
  overflow: visible;
  white-space: normal;
}
</style>
