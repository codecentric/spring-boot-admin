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
  <table class="table is-hoverable is-fullwidth">
    <tbody>
      <tr v-for="logger in loggers.slice(0, this.visibleLimit)" :key="logger.name">
        <td>
          <span class="is-breakable" v-text="logger.name" />&nbsp;
          <span class="tag is-primary is-uppercase" v-if="logger.isNew" v-text="$t('instances.loggers.new')" />
          <sba-logger-control class="is-pulled-right"
                              :level-options="levels"
                              :value="logger.level"
                              :status="loggersStatus[logger.name]"
                              :allow-reset="logger.name !== 'ROOT'"
                              @input="level => $emit('configureLogger', {logger, level})"
          />

          <p
            class="has-text-danger"
            v-if="loggersStatus[logger.name] && loggersStatus[logger.name].status === 'failed'"
          >
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
            <span v-text="$t('instances.loggers.setting_loglevel_failed', {logger: logger.name, loglevel: loggersStatus[logger.name].level})" />
          </p>
        </td>
      </tr>
      <tr v-if="loggers.length === 0">
        <td class="is-muted" colspan="5" v-text="$t('instances.loggers.no_loggers_found')" />
      </tr>
    </tbody>
    <infinite-loading ref="infinite" @infinite="increaseScroll">
      <div slot="no-results" />
      <div slot="no-more" />
    </infinite-loading>
  </table>
</template>
<script>
    import InfiniteLoading from 'vue-infinite-loading'
    import SbaLoggerControl from './logger-control'

    export default {
    components: {InfiniteLoading, SbaLoggerControl},
    props: {
      levels: {
        type: Array,
        required: true
      },
      loggers: {
        type: Array,
        required: true
      },
      loggersStatus: {
        type: Object,
        required: true
      }
    },
    data: () => ({
      visibleLimit: 25
    }),
    watch: {
      loggers: {
        handler() {
          this.$refs.infinite.stateChanger.reset();
        }
      },
    },
    methods: {
      increaseScroll(state) {
        if (this.visibleLimit < this.loggers.length) {
          this.visibleLimit += 25;
          state.loaded();
        } else {
          state.complete();
        }
      }
    }
  }
</script>
