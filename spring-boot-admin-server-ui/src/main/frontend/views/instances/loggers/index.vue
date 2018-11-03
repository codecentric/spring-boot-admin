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
  <section class="section" :class="{ 'is-loading' : !hasLoaded }">
    <template v-if="hasLoaded">
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
            Fetching loggers failed.
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <template v-if="loggerConfig">
        <div class="field-body">
          <div class="field has-addons">
            <p class="control is-expanded">
              <input class="input" type="search" placeholder="name filter" v-model="filter">
            </p>
            <p class="control">
              <span class="button is-static">
                <span v-text="filteredLoggers.length" />
                /
                <span v-text="loggerConfig.loggers.length" />
              </span>
            </p>
          </div>
        </div>
        <div class="field-body">
          <div class="field is-narrow">
            <div class="control">
              <label class="checkbox">
                <input type="checkbox" v-model="showClassLoggersOnly">
                class only
              </label>
            </div>
          </div>
          <div class="field is-narrow">
            <div class="control">
              <label class="checkbox">
                <input type="checkbox" v-model="showConfiguredLoggersOnly">
                configured
              </label>
            </div>
          </div>
        </div>
      </template>
      <table v-if="loggerConfig" class="table is-hoverable is-fullwidth">
        <tbody>
          <tr v-for="logger in limitedLoggers" :key="logger.name">
            <td>
              <span class="is-breakable" v-text="logger.name" />
              <span class="has-text-danger" v-if="logger.name in failed">
                <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
                <span v-text="`Configuring ${failed[logger.name]} failed.`" />
              </span>
              <sba-logger-control class="is-pulled-right"
                                  :level-options="levels"
                                  :effective-level="logger.effectiveLevel"
                                  :configured-level="logger.configuredLevel"
                                  :is-loading="loading[logger.name]"
                                  :has-failed="failed[logger.name]"
                                  :allow-reset="logger.name !== 'ROOT'"
                                  @input="level => configureLogger(logger, level)"
              />
            </td>
          </tr>
          <tr v-if="limitedLoggers.length === 0">
            <td class="is-muted" colspan="5">No loggers found.
            </td>
          </tr>
        </tbody>
      </table>
    </template>
  </section>
</template>

<script>
  import Instance from '@/services/instance';
  import sbaLoggerControl from './logger-control';

  const isClassName = name => /\.[A-Z]/.test(name);

  const addToFilter = (oldFilter, addedFilter) =>
    !oldFilter
      ? addedFilter
      : (val, key) => oldFilter(val, key) && addedFilter(val, key);

  export default {
    // eslint-disable-next-line vue/no-unused-components
    components: {sbaLoggerControl},
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
      filter: '',
      showClassLoggersOnly: false,
      showConfiguredLoggersOnly: false,
      visibleLimit: 25,
      loading: {},
      failed: {}
    }),
    computed: {
      levels() {
        return this.loggerConfig.levels;
      },
      limitedLoggers() {
        if (this.visibleLimit > 0) {
          return this.filteredLoggers.slice(0, this.visibleLimit);
        } else {
          return this.filteredLoggers;
        }
      },
      filteredLoggers() {
        let filterFn = this.getFilterFn();
        return filterFn ? this.loggerConfig.loggers.filter(filterFn) : this.loggerConfig.loggers;
      }
    },
    methods: {
      async configureLogger(logger, level) {
        const vm = this;
        const setLoadingHandle = setTimeout(() => vm.$set(vm.loading, logger.name, level), 150);
        try {
          await this.instance.configureLogger(logger.name, level);
          vm.$delete(vm.failed, logger.name, level)
        } catch (error) {
          console.warn(`Configuring logger '${logger.name}' failed:`, error);
          vm.$set(vm.failed, logger.name, level);
        }

        try {
          await this.fetchLoggers();
        } finally {
          vm.$delete(vm.loading, logger.name);
          clearTimeout(setLoadingHandle);
        }
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
      onScroll() {
        if (this.loggerConfig && this.$el.getBoundingClientRect().bottom - 400 <= window.innerHeight && this.visibleLimit < this.filteredLoggers.length) {
          this.visibleLimit += 25;
        }
      },
      getFilterFn() {
        let filterFn = null;

        if (this.showClassLoggersOnly) {
          filterFn = addToFilter(filterFn, logger => isClassName(logger.name));
        }

        if (this.showConfiguredLoggersOnly) {
          filterFn = addToFilter(filterFn, logger => !!logger.configuredLevel);
        }

        if (this.filter) {
          const normalizedFilter = this.filter.toLowerCase();
          filterFn = addToFilter(filterFn, logger => logger.name.toLowerCase().includes(normalizedFilter));
        }

        return filterFn;
      }
    },
    created() {
      this.fetchLoggers();
    },
    mounted() {
      window.addEventListener('scroll', this.onScroll);
    },
    beforeDestroy() {
      window.removeEventListener('scroll', this.onScroll);
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
</style>
