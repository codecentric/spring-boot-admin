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
  <sba-instance-section :loading="!hasLoaded" :error="error">
    <template #before>
      <sba-sticky-subnav>
        <div class="flex gap-2">
          <sba-toggle-scope-button
            v-if="instanceCount >= 1"
            v-model="scope"
            :show-info="false"
            :instance-count="instanceCount"
            @change-scope="$emit('changeScope', $event)"
          />

          <div class="flex-1">
            <sba-input
              v-model="filter.name"
              name="filter"
              type="search"
              :placeholder="$t('term.filter')"
            >
              <template #prepend>
                <font-awesome-icon icon="filter" />
              </template>
              <template #append>
                <span v-text="filteredLoggers.length" /> /
                <span v-text="loggerConfig.loggers.length" />
              </template>
            </sba-input>
          </div>

          <!-- FILTER -->
          <div>
            <div class="flex items-start">
              <div class="flex items-center h-5">
                <input
                  id="classOnly"
                  v-model="filter.classOnly"
                  name="wraplines"
                  type="checkbox"
                  class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded"
                />
              </div>
              <div class="ml-3 text-sm">
                <label
                  for="classOnly"
                  class="font-medium text-gray-700"
                  v-text="$t('instances.loggers.filter.class_only')"
                />
              </div>
            </div>

            <div class="flex items-start">
              <div class="flex items-center h-5">
                <input
                  id="configuredOnly"
                  v-model="filter.configuredOnly"
                  name="wraplines"
                  type="checkbox"
                  class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded"
                />
              </div>
              <div class="ml-3 text-sm">
                <label
                  for="configuredOnly"
                  class="font-medium text-gray-700"
                  v-text="$t('instances.loggers.filter.configured')"
                />
              </div>
            </div>
          </div>
          <!-- // FILTER -->
        </div>
      </sba-sticky-subnav>
    </template>

    <sba-panel>
      <div v-if="failedInstances > 0" class="message is-warning">
        <div class="message-body">
          <sba-alert
            severity="WARN"
            :title="
              $t('instances.loggers.fetch_failed_some_instances', {
                failed: failedInstances,
                count: instanceCount,
              })
            "
          />
        </div>
      </div>

      <loggers-list
        :levels="loggerConfig.levels"
        :loggers="filteredLoggers"
        :loggers-status="loggersStatus"
        @configure-logger="
          ({ logger, level }) => configureLogger(logger, level)
        "
      />
    </sba-panel>
  </sba-instance-section>
</template>

<script>
import SbaAlert from '@/components/sba-alert';

import { finalize, from, listen } from '@/utils/rxjs';
import LoggersList from '@/views/instances/loggers/loggers-list';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

const isClassName = (name) => /\.[A-Z]/.test(name);

const addToFilter = (oldFilter, addedFilter) =>
  !oldFilter
    ? addedFilter
    : (val, key) => oldFilter(val, key) && addedFilter(val, key);

const addLoggerCreationEntryIfLoggerNotPresent = (nameFilter, loggers) => {
  if (nameFilter && !loggers.some((logger) => logger.name === nameFilter)) {
    loggers.unshift({
      level: [
        {
          configuredLevel: null,
          effectiveLevel: null,
          instanceId: null,
        },
      ],
      name: nameFilter,
      isNew: true,
    });
  }
};

export default {
  components: { SbaAlert, SbaInstanceSection, LoggersList },
  props: {
    instanceCount: {
      type: Number,
      required: true,
    },
    loggersService: {
      type: Object,
      required: true,
    },
  },
  emits: ['changeScope'],
  data() {
    return {
      hasLoaded: false,
      error: null,
      failedInstances: 0,
      loggerConfig: { loggers: [], levels: [] },
      filter: {
        name: '',
        classOnly: false,
        configuredOnly: false,
      },
      loggersStatus: {},
      scope: this.instanceCount > 1 ? 'application' : 'instance',
    };
  },
  computed: {
    filteredLoggers() {
      const filterFn = this.getFilterFn();
      const filteredLoggers = filterFn
        ? this.loggerConfig.loggers.filter(filterFn)
        : this.loggerConfig.loggers;
      addLoggerCreationEntryIfLoggerNotPresent(
        this.filter.name,
        filteredLoggers
      );
      return filteredLoggers;
    },
  },
  watch: {
    loggersService: {
      immediate: true,
      handler() {
        return this.fetchLoggers();
      },
    },
  },
  methods: {
    configureLogger(logger, level) {
      const vm = this;
      from(vm.loggersService.configureLogger(logger.name, level))
        .pipe(
          listen(
            (status) => (vm.loggersStatus[logger.name] = { level, status })
          ),
          finalize(() => vm.fetchLoggers())
        )
        .subscribe({
          error: (error) =>
            console.warn(`Configuring logger '${logger.name}' failed:`, error),
        });
    },
    async fetchLoggers() {
      this.error = null;
      this.failedInstances = 0;
      try {
        const { errors, ...loggerConfig } =
          await this.loggersService.fetchLoggers();
        this.loggerConfig = Object.freeze(loggerConfig);
        this.failedInstances = errors.length;
      } catch (error) {
        console.warn('Fetching loggers failed:', error);
        this.error = error;
      }
      this.hasLoaded = true;
    },
    getFilterFn() {
      let filterFn = null;

      if (this.filter.classOnly) {
        filterFn = addToFilter(filterFn, (logger) => isClassName(logger.name));
      }

      if (this.filter.configuredOnly) {
        filterFn = addToFilter(filterFn, (logger) =>
          logger.level.some((l) => Boolean(l.configuredLevel))
        );
      }

      if (this.filter.name) {
        const normalizedFilter = this.filter.name.toLowerCase();
        filterFn = addToFilter(filterFn, (logger) =>
          logger.name.toLowerCase().includes(normalizedFilter)
        );
      }

      return filterFn;
    },
  },
};
</script>
