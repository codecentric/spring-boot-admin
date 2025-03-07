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
  <sba-instance-section :error="error" :loading="!hasLoaded">
    <template #before>
      <sba-sticky-subnav>
        <div class="flex gap-2">
          <sba-toggle-scope-button
            v-if="instanceCount > 1"
            v-model="scope"
            :instance-count="instanceCount"
            :show-info="false"
          />

          <div class="flex-1">
            <sba-input
              v-model="filter.name"
              :placeholder="$t('term.filter')"
              name="filter"
              type="search"
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
                  class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded"
                  name="wraplines"
                  type="checkbox"
                />
              </div>
              <div class="ml-3 text-sm">
                <label
                  class="font-medium text-gray-700"
                  for="classOnly"
                  v-text="t('instances.loggers.filter.class_only')"
                />
              </div>
            </div>

            <div class="flex items-start">
              <div class="flex items-center h-5">
                <input
                  id="configuredOnly"
                  v-model="filter.configuredOnly"
                  class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded"
                  name="wraplines"
                  type="checkbox"
                />
              </div>
              <div class="ml-3 text-sm">
                <label
                  class="font-medium text-gray-700"
                  for="configuredOnly"
                  v-text="t('instances.loggers.filter.configured')"
                />
              </div>
            </div>
          </div>
          <!-- // FILTER -->
        </div>
      </sba-sticky-subnav>
    </template>

    <sba-panel
      v-if="loggerConfig.groups"
      :title="t('instances.loggers.group.label')"
    >
      <loggers-list
        :infinite-scroll="false"
        :levels="loggerConfig.levels"
        :loggers="loggerConfig.groups"
        :loggers-status="loggersStatus"
        @configure-logger="
          ({ logger, level }) => configureLogger(logger, level)
        "
      />
    </sba-panel>

    <sba-panel :title="t('instances.loggers.label')">
      <div v-if="failedInstances > 0" class="message is-warning">
        <div class="message-body">
          <sba-alert
            :title="
              t('instances.loggers.fetch_failed_some_instances', {
                failed: failedInstances,
                count: instanceCount,
              })
            "
            severity="WARN"
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

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import { ActionScope } from '@/components/ActionScope';
import SbaAlert from '@/components/sba-alert';
import SbaToggleScopeButton from '@/components/sba-toggle-scope-button.vue';

import { finalize, from, listen } from '@/utils/rxjs';
import LoggersList from '@/views/instances/loggers/loggers-list';
import {
  ApplicationLoggers,
  InstanceLoggers,
} from '@/views/instances/loggers/service';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

const { t } = useI18n();

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

const { loggersService, instanceCount = 0 } = defineProps<{
  instanceCount?: number;
  loggersService: InstanceLoggers | ApplicationLoggers;
}>();

const emit = defineEmits<{
  (
    e: 'changeScope',
    scope: ActionScope.APPLICATION | ActionScope.INSTANCE,
  ): void;
}>();

const hasLoaded = ref(false);
const error = ref(null);
const failedInstances = ref(0);
const loggerConfig = ref({ loggers: [], levels: [], groups: [] });
const loggersStatus = ref({});
const filter = reactive({
  name: '',
  classOnly: false,
  configuredOnly: false,
});
const scope = ref(ActionScope.APPLICATION);

const filteredLoggers = computed(() => {
  const filterFn = getFilterFn();
  const filteredLoggers = filterFn
    ? loggerConfig.value.loggers.filter(filterFn)
    : loggerConfig.value.loggers;
  addLoggerCreationEntryIfLoggerNotPresent(filter.name, filteredLoggers);
  return filteredLoggers;
});

watch(scope, (scope) => {
  emit('changeScope', scope);
});

watch(
  () => loggersService,
  () => {
    if (loggersService) {
      fetchLoggers();
    }
  },
  { immediate: true },
);

function configureLogger(logger, level) {
  from(loggersService.configureLogger(logger.name, level))
    .pipe(
      listen(
        (status) => (loggersStatus.value[logger.name] = { level, status }),
      ),
      finalize(() => fetchLoggers()),
    )
    .subscribe({
      error: (error) =>
        console.warn(`Configuring logger '${logger.name}' failed:`, error),
    });
}

async function fetchLoggers() {
  error.value = null;
  failedInstances.value = 0;
  try {
    let { errors, ...rest } = await loggersService.fetchLoggers();
    loggerConfig.value = Object.freeze(rest);
    failedInstances.value = errors.length;
  } catch (e) {
    console.warn('Fetching loggers failed:', e);
    error.value = e;
  }
  hasLoaded.value = true;
}

function getFilterFn() {
  let filterFn = null;

  if (filter.classOnly) {
    filterFn = addToFilter(filterFn, (logger) => isClassName(logger.name));
  }

  if (filter.configuredOnly) {
    filterFn = addToFilter(filterFn, (logger) =>
      logger.level.some((l) => Boolean(l.configuredLevel)),
    );
  }

  if (filter.name) {
    const normalizedFilter = filter.name.toLowerCase();
    filterFn = addToFilter(filterFn, (logger) =>
      logger.name.toLowerCase().includes(normalizedFilter),
    );
  }

  return filterFn;
}
</script>
