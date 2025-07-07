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
  <LineChart
    :config="config"
    :data="data"
    :datasets="datasets"
    label="timestamp"
  />
</template>

<script setup lang="ts">
import moment from 'moment';
import prettyBytes from 'pretty-bytes';
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import LineChart from '@/views/instances/details/LineChart';

const { t } = useI18n();

const { data } = defineProps<{
  data: Array<any>;
}>();

const datasets = computed(() => {
  const hasMetaspace = Object.values(data).some((d) => d.metaspace !== null);

  const _datasets: Record<string, { label: string }> = {
    used: {
      label: 'instances.details.memory.used',
    },
    committed: {
      label: 'instances.details.memory.committed',
    },
  };

  if (hasMetaspace) {
    _datasets.metaspace = {
      label: 'instances.details.memory.metaspace',
    };
  }

  return _datasets;
});

const config = {
  options: {
    plugins: {
      tooltip: {
        callbacks: {
          title: (ctx) => {
            return prettyBytes(ctx[0].parsed.y);
          },
          label: (ctx) => {
            return t(ctx.dataset.label);
          },
        },
      },
    },
    scales: {
      y: {
        ticks: {
          callback: (label) => prettyBytes(label),
        },
      },
      x: {
        ticks: {
          callback: (label) => moment(label).format('HH:mm:ss'),
        },
      },
    },
  },
};
</script>
