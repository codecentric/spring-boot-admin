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
  <table class="w-full">
    <tbody>
      <tr v-for="logger in loggers.slice(0, visibleLimit)" :key="logger.name">
        <td class="w-9/12">
          <span class="break-all" v-text="logger.name" />&nbsp;
          <sba-tag
            v-if="logger.isNew"
            class="tag is-primary is-uppercase"
            :value="$t('instances.loggers.new')"
          />
        </td>
        <td class="w-1/4">
          <sba-logger-control
            class="is-pulled-right"
            :level-options="levels"
            :value="logger.level"
            :status="loggersStatus[logger.name]"
            :allow-reset="logger.name !== 'ROOT'"
            @input="(level) => $emit('configureLogger', { logger, level })"
          />

          <p
            v-if="
              loggersStatus[logger.name] &&
              loggersStatus[logger.name].status === 'failed'
            "
            class="has-text-danger"
          >
            <font-awesome-icon
              class="has-text-danger"
              icon="exclamation-triangle"
            />
            <span
              v-text="
                $t('instances.loggers.setting_loglevel_failed', {
                  logger: logger.name,
                  loglevel: loggersStatus[logger.name].level,
                })
              "
            />
          </p>
        </td>
      </tr>
      <tr v-if="loggers.length === 0">
        <td
          class="is-muted"
          colspan="5"
          v-text="$t('instances.loggers.no_loggers_found')"
        />
      </tr>
    </tbody>

    <InfiniteLoading
      v-if="infiniteScroll && loggers.length !== 0"
      ref="infiniteLoading"
      :identifier="loggers"
      @infinite="increaseScroll"
    >
      <template #complete>
        <span />
      </template>
    </InfiniteLoading>
  </table>
</template>

<script setup lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import InfiniteLoading from 'v3-infinite-loading';
import { ref } from 'vue';

import 'v3-infinite-loading/lib/style.css';

import SbaLoggerControl from '@/views/instances/loggers/logger-control';

const { loggers, infiniteScroll = true } = defineProps<{
  levels: string[];
  loggers: any[];
  loggersStatus: Record<string, any>;
  infiniteScroll?: boolean;
}>();

defineEmits<{
  (event: 'configureLogger', payload: { logger: any; level: string }): void;
}>();

const visibleLimit = ref(25);

function increaseScroll($state) {
  if (visibleLimit.value <= loggers.length) {
    visibleLimit.value += 25;
    $state.loaded();
  } else {
    $state.complete();
  }
}
</script>
