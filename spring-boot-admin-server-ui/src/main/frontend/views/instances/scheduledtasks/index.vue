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
    <sba-panel>
      <div v-if="!hasData" class="message is-warning">
        <div
          class="message-body"
          v-text="$t('instances.scheduledtasks.no_scheduledtasks')"
        />
      </div>

      <div class="flex flex-col">
        <template v-if="hasCronData">
          <sba-panel :title="$t('instances.scheduledtasks.cron.title')">
            <table class="table w-full">
              <thead>
                <tr>
                  <th
                    class="text-left"
                    v-text="$t('instances.scheduledtasks.cron.runnable')"
                  />
                  <th v-text="$t('instances.scheduledtasks.cron.expression')" />
                  <th
                    v-text="$t('instances.scheduledtasks.cron.next_execution')"
                  />
                  <th
                    v-text="$t('instances.scheduledtasks.cron.last_execution')"
                  />
                  <th
                    v-text="
                      $t('instances.scheduledtasks.cron.last_execution_status')
                    "
                  />
                </tr>
              </thead>
              <tbody v-for="task in cron" :key="task.runnable.target">
                <tr>
                  <td v-text="task.runnable.target" />
                  <td class="font-mono text-sm" v-text="task.expression" />
                  <scheduled-task-executions :task="task" />
                </tr>
              </tbody>
            </table>
          </sba-panel>
        </template>

        <template v-if="hasFixedDelayData">
          <sba-panel :title="$t('instances.scheduledtasks.fixed_delay.title')">
            <table class="table w-full">
              <thead>
                <tr>
                  <th
                    v-text="$t('instances.scheduledtasks.fixed_delay.runnable')"
                  />
                  <th
                    v-text="
                      $t(
                        'instances.scheduledtasks.fixed_delay.initial_delay_ms',
                      )
                    "
                  />
                  <th
                    v-text="
                      $t('instances.scheduledtasks.fixed_delay.interval_ms')
                    "
                  />
                  <th
                    v-text="
                      $t('instances.scheduledtasks.fixed_delay.next_execution')
                    "
                  />
                  <th
                    v-text="
                      $t('instances.scheduledtasks.fixed_delay.last_execution')
                    "
                  />
                  <th
                    v-text="
                      $t(
                        'instances.scheduledtasks.fixed_delay.last_execution_status',
                      )
                    "
                  />
                </tr>
              </thead>
              <tbody v-for="task in fixedDelay" :key="task.runnable.target">
                <tr>
                  <td v-text="task.runnable.target" />
                  <td v-text="task.initialDelay" />
                  <td v-text="task.interval" />
                  <scheduled-task-executions :task="task" />
                </tr>
              </tbody>
            </table>
          </sba-panel>
        </template>

        <template v-if="hasFixedRateData">
          <sba-panel :title="$t('instances.scheduledtasks.fixed_rate.title')">
            <table class="table w-full">
              <thead>
                <tr>
                  <th
                    v-text="$t('instances.scheduledtasks.fixed_delay.runnable')"
                  />
                  <th
                    v-text="
                      $t(
                        'instances.scheduledtasks.fixed_delay.initial_delay_ms',
                      )
                    "
                  />
                  <th
                    v-text="
                      $t('instances.scheduledtasks.fixed_delay.interval_ms')
                    "
                  />
                  <th
                    v-text="
                      $t('instances.scheduledtasks.fixed_delay.next_execution')
                    "
                  />
                  <th
                    v-text="
                      $t('instances.scheduledtasks.fixed_delay.last_execution')
                    "
                  />
                  <th
                    v-text="
                      $t(
                        'instances.scheduledtasks.fixed_delay.last_execution_status',
                      )
                    "
                  />
                </tr>
              </thead>
              <tbody v-for="task in fixedRate" :key="task.runnable.target">
                <tr>
                  <td v-text="task.runnable.target" />
                  <td v-text="task.initialDelay" />
                  <td v-text="task.interval" />
                  <scheduled-task-executions :task="task" />
                </tr>
              </tbody>
            </table>
          </sba-panel>
        </template>
      </div>
    </sba-panel>
  </sba-instance-section>
</template>

<script>
import SbaPanel from '@/components/sba-panel.vue';

import Instance from '@/services/instance';
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
