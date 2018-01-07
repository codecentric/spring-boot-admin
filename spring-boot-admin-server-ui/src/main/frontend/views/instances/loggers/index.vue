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
    <section class="section" :class="{ 'is-loading' : !loggerConfig }">
        <div class="container" v-if="loggerConfig">
            <div class="content">
                <div class="field-body">
                    <div class="field has-addons">
                        <p class="control is-expanded">
                            <input class="input" type="text" placeholder="name" v-model="filter">
                        </p>
                        <p class="control">
                    <span class="button is-static">
                        <span v-text="filteredLoggers.length"></span>
                        /
                        <span v-text="loggerConfig.loggers.length"></span>
                    </span>
                        </p>
                    </div>
                </div>
                <div class="field-body">
                    <div class="field is-narrow">
                        <div class="control">
                            <label class="checkbox">
                                <input type="checkbox" v-model="showClassLoggersOnly">
                                class
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
            </div>
            <table class="table is-hoverable is-fullwidth">
                <tbody>
                <tr v-for="logger in limitedLoggers" :key="logger.name">
                    <td>
                        <span class="is-breakable" v-text="logger.name"></span>
                        <sba-logger-control class="is-pulled-right"
                                            :level-options="levels"
                                            :effective-level="logger.effectiveLevel"
                                            :configured-level="logger.configuredLevel"
                                            :is-loading="loading[logger.name]"
                                            :allow-reset="logger.name !== 'ROOT'"
                                            @input="level => configureLogger(logger, level)">
                        </sba-logger-control>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </section>
</template>

<script>
  import sbaLoggerControl from './logger-control';

  const isPackageName = (name) => {
    const i = name.lastIndexOf('.') + 1;
    return name.charAt(i) !== name.charAt(i).toUpperCase();
  };

  const addToFilter = (oldFilter, addedFilter) =>
    !oldFilter
      ? addedFilter
      : (val, key) => oldFilter(val, key) && addedFilter(val, key);

  export default {
    components: {
      sbaLoggerControl
    },
    props: ['instance'],
    data: () => ({
      loggerConfig: null,
      filter: '',
      showClassLoggersOnly: true,
      showConfiguredLoggersOnly: false,
      visibleLimit: 25,
      loading: {}
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
        const handle = setTimeout(() => vm.$set(vm.loading, logger.name, level), 150);
        await this.instance.configureLogger(logger.name, level);
        await this.fetchLoggers();
        vm.$delete(vm.loading, logger.name);
        clearTimeout(handle);
      },
      async fetchLoggers() {
        if (this.instance) {
          const res = await this.instance.fetchLoggers();
          this.loggerConfig = res.data;
        }
      },
      onScroll() {
        if (this.loggerConfig
          && (this.$el.getBoundingClientRect().bottom - 400) <= window.innerHeight
          && this.visibleLimit < this.filteredLoggers.length) {
          this.visibleLimit += 25;
        }
      },
      getFilterFn() {
        let filterFn = null;

        if (this.showClassLoggersOnly) {
          filterFn = addToFilter(filterFn, (logger) => !isPackageName(logger.name));
        }

        if (this.showConfiguredLoggersOnly) {
          filterFn = addToFilter(filterFn, (logger) => !!logger.configuredLevel);
        }

        if (this.filter) {
          const normalizedFilter = this.filter.toLowerCase();
          filterFn = addToFilter(filterFn, (logger) => logger.name.toLowerCase().indexOf(normalizedFilter) >= 0);
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
    watch: {
      instance() {
        this.fetchLoggers();
      }
    }
  }
</script>

<style lang="scss">
</style>