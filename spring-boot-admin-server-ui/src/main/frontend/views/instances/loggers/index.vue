<!--
  - Copyright 2014-2019 the original author or authors.
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
  <section class="section" :class="{ 'is-loading' : !hasLoaded }">
    <div v-if="error" class="message is-danger">
      <div class="message-body">
        <strong>
          <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
          Fetching loggers failed.
        </strong>
        <p v-text="error.message" />
      </div>
    </div>
    <div class="loggers__filters" v-sticks-below="['#navigation']" v-if="loggerConfig">
      <div class="field-body">
        <div class="field has-addons">
          <p class="control is-expanded has-icons-left">
            <input
              class="input"
              type="search"
              v-model="filter.name"
            >
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
      </div>
      <div class="field-body">
        <div class="field is-narrow">
          <div class="control">
            <label class="checkbox">
              <input type="checkbox" v-model="filter.classOnly">
              class only
            </label>
          </div>
        </div>
        <div class="field is-narrow">
          <div class="control">
            <label class="checkbox">
              <input type="checkbox" v-model="filter.configuredOnly">
              configured
            </label>
          </div>
        </div>
      </div>
    </div>
    <table v-if="loggerConfig" class="table is-hoverable is-fullwidth">
      <tbody>
        <tr v-for="logger in this.filteredLoggers.slice(0, this.visibleLimit)" :key="logger.name">
          <td>
            <span class="is-breakable" v-text="logger.name" />

            <sba-logger-control class="is-pulled-right"
                                :level-options="levels"
                                :effective-level="logger.effectiveLevel"
                                :configured-level="logger.configuredLevel"
                                :status="loggerStatus[logger.name]"
                                :allow-reset="logger.name !== 'ROOT'"
                                @input="level => configureLogger(logger, level)"
            />

            <p
              class="has-text-danger"
              v-if="loggerStatus[logger.name] && loggerStatus[logger.name].status === 'failed'"
            >
              <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
              <span v-text="`Setting ${logger.name} to '${loggerStatus[logger.name].level}' failed.`" />
            </p>
          </td>
        </tr>
        <tr v-if="filteredLoggers.length === 0">
          <td class="is-muted" colspan="5">
            No loggers found.
          </td>
        </tr>
      </tbody>
    </table>
    <infinite-loading ref="infinite" v-if="loggerConfig" @infinite="increaseScroll">
      <div slot="no-results" />
    </infinite-loading>
  </section>
</template>

<script>
  import sticksBelow from '@/directives/sticks-below';
  import Instance from '@/services/instance';
  import {finalize, from, listen} from '@/utils/rxjs';
  import InfiniteLoading from 'vue-infinite-loading';
  import sbaLoggerControl from './logger-control';

  const isClassName = name => /\.[A-Z]/.test(name);

  const addToFilter = (oldFilter, addedFilter) =>
    !oldFilter
      ? addedFilter
      : (val, key) => oldFilter(val, key) && addedFilter(val, key);

  export default {
    components: {sbaLoggerControl, InfiniteLoading},
    directives: {sticksBelow},
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    data: () => ({
      hasLoaded: false,
      error: null,
      loggerConfig: null,
      filter: {
        name: '',
        classOnly: false,
        configuredOnly: false,
      },
      visibleLimit: 20,
      loggerStatus: {}
    }),
    computed: {
      levels() {
        return this.loggerConfig.levels;
      },
      filteredLoggers() {
        let filterFn = this.getFilterFn();
        return filterFn ? this.loggerConfig.loggers.filter(filterFn) : this.loggerConfig.loggers;
      }
    },
    watch: {
      filter: {
        deep: true,
        handler() {
          this.$refs.infinite.stateChanger.reset();
        }
      }
    },
    methods: {
      configureLogger(logger, level) {
        const vm = this;
        from(vm.instance.configureLogger(logger.name, level))
          .pipe(
            listen(status => vm.$set(vm.loggerStatus, logger.name, {level, status})),
            finalize(() => vm.fetchLoggers())
          )
          .subscribe({
            error: (error) => console.warn(`Configuring logger '${logger.name}' failed:`, error)
          });
      },
      async fetchLoggers() {
        this.error = null;
        try {
          const res = await this.instance.fetchLoggers();
          this.loggerConfig = res.data;
        } catch (error) {
          console.warn('Fetching loggers failed:', error);
          this.error = error;
        }
        this.hasLoaded = true;
      },
      increaseScroll(state) {
        if (this.visibleLimit < this.filteredLoggers.length) {
          this.visibleLimit += 25;
          state.loaded();
        } else {
          state.complete();
        }
      },
      getFilterFn() {
        let filterFn = null;

        if (this.filter.classOnly) {
          filterFn = addToFilter(filterFn, logger => isClassName(logger.name));
        }

        if (this.filter.configuredOnly) {
          filterFn = addToFilter(filterFn, logger => Boolean(logger.configuredLevel));
        }

        if (this.filter.name) {
          const normalizedFilter = this.filter.name.toLowerCase();
          filterFn = addToFilter(filterFn, logger => logger.name.toLowerCase().includes(normalizedFilter));
        }

        return filterFn;
      }
    },
    created() {
      this.fetchLoggers();
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/loggers',
        parent: 'instances',
        path: 'loggers',
        component: this,
        label: 'Loggers',
        group: 'Logging',
        order: 300,
        isEnabled: ({instance}) => instance.hasEndpoint('loggers')
      });
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .loggers__filters {
    background-color: $white;
    z-index: 10;
    padding: 0.5em 1em;
  }
</style>
