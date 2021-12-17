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
  <section :class="{ 'is-loading' : !hasLoaded }" class="section">
    <sba-alert v-if="error" :error="error" :title="$t('instances.loggers.fetch_failed')" />

    <div v-sticks-below="['#navigation']" class="loggers__header">
      <div class="field is-grouped">
        <div class="control">
          <sba-toggle-scope-button v-if="instanceCount > 1"
                                   v-model="scope"
                                   :instance-count="instanceCount"
          />
        </div>
        <div class="control is-expanded">
          <div class="field has-addons">
            <p class="control is-expanded has-icons-left">
              <input v-model="filter.name" class="input" type="search">
              <span class="icon is-small is-left">
                <font-awesome-icon icon="filter" />
              </span>
            </p>
            <p class="control">
              <span class="button is-static">
                <span v-text="filteredLoggers.length" /> / <span v-text="loggerConfig.loggers.length" />
              </span>
            </p>
          </div>

          <div class="field is-grouped">
            <div class="control">
              <label class="checkbox">
                <input v-model="filter.classOnly" type="checkbox">
                {{ $t('instances.loggers.filter.class_only') }}
              </label>
            </div>
            <div class="control">
              <label class="checkbox">
                <input v-model="filter.configuredOnly" type="checkbox">
                {{ $t('instances.loggers.filter.configured') }}
              </label>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="failedInstances > 0" class="message is-warning">
      <div class="message-body">
        <font-awesome-icon class="has-text-warning" icon="exclamation-triangle" />
        <span
          v-text="$t('instances.loggers.fetch_failed_some_instances', {failed: failedInstances, count: instanceCount})"
        />
      </div>
    </div>

    <loggers-list
      :levels="loggerConfig.levels"
      :loggers="filteredLoggers"
      :loggers-status="loggersStatus"
      @configureLogger="({logger, level}) => configureLogger(logger, level)"
    />
  </section>
</template>

<script>
import sticksBelow from '@/directives/sticks-below';
import {finalize, from, listen} from '@/utils/rxjs';
import LoggersList from './loggers-list';
import SbaToggleScopeButton from '@/components/sba-toggle-scope-button';

const isClassName = name => /\.[A-Z]/.test(name);

const addToFilter = (oldFilter, addedFilter) =>
  !oldFilter
    ? addedFilter
    : (val, key) => oldFilter(val, key) && addedFilter(val, key);

const addLoggerCreationEntryIfLoggerNotPresent = (nameFilter, loggers) => {
  if (nameFilter && !loggers.some(logger => logger.name === nameFilter)) {
    loggers.unshift({
      level: [{
        configuredLevel: null,
        effectiveLevel: null,
        instanceId: null
      }],
      name: nameFilter,
      isNew: true
    })
  }
};

export default {
  components: {SbaToggleScopeButton, LoggersList},
  directives: {sticksBelow},
  props: {
    instanceCount: {
      type: Number,
      required: true
    },
    loggersService: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      hasLoaded: false,
      error: null,
      failedInstances: 0,
      loggerConfig: {loggers: [], levels: []},
      filter: {
        name: '',
        classOnly: false,
        configuredOnly: false,
      },
      loggersStatus: {},
      scope: this.instanceCount > 1 ? 'application' : 'instance'
    }
  },
  computed: {
    filteredLoggers() {
      const filterFn = this.getFilterFn();
      const filteredLoggers = filterFn ? this.loggerConfig.loggers.filter(filterFn) : this.loggerConfig.loggers;
      addLoggerCreationEntryIfLoggerNotPresent(this.filter.name, filteredLoggers);
      return filteredLoggers;
    }
  },
  watch: {
    loggersService: {
      immediate: true,
      handler() {
        return this.fetchLoggers();
      }
    }
  },
  methods: {
    configureLogger(logger, level) {
      const vm = this;
      from(vm.loggersService.configureLogger(logger.name, level))
        .pipe(
          listen(status => vm.$set(vm.loggersStatus, logger.name, {level, status})),
          finalize(() => vm.fetchLoggers())
        )
        .subscribe({
          error: (error) => console.warn(`Configuring logger '${logger.name}' failed:`, error)
        });
    },
    async fetchLoggers() {
      this.error = null;
      this.failedInstances = 0;
      try {
        const {errors, ...loggerConfig} = await this.loggersService.fetchLoggers();
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
        filterFn = addToFilter(filterFn, logger => isClassName(logger.name));
      }

      if (this.filter.configuredOnly) {
        filterFn = addToFilter(filterFn, logger => logger.level.some(l => Boolean(l.configuredLevel)));
      }

      if (this.filter.name) {
        const normalizedFilter = this.filter.name.toLowerCase();
        filterFn = addToFilter(filterFn, logger => logger.name.toLowerCase().includes(normalizedFilter));
      }

      return filterFn;
    }
  }
}
</script>

<style lang="scss">
@import "~@/assets/css/utilities";

.loggers {
  &__header {
    background-color: $white;
    z-index: 10;
    padding: 0.5em 1em;
  }

  &__toggle-scope {
    width: 10em;
  }
}
</style>
