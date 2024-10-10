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
  <loggers
    :instance-count="application.instances.length"
    :loggers-service="service"
    @change-scope="changeScope"
  />
</template>

<script setup lang="ts">
import { ActionScope } from '@/components/ActionScope';

import Application from '@/services/application';
import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import Loggers from '@/views/instances/loggers/loggers';
import {
  ApplicationLoggers,
  InstanceLoggers,
} from '@/views/instances/loggers/service';
import { computed, ref } from 'vue';

const { instance, application } = defineProps<{
  instance: Instance;
  application: Application;
}>();

const scope = ref(
  application.instances.length > 1
    ? ActionScope.APPLICATION
    : ActionScope.INSTANCE,
);

const service = computed(() =>
  scope.value === ActionScope.INSTANCE
    ? new InstanceLoggers(instance)
    : new ApplicationLoggers(application),
);

function changeScope(newScope) {
  scope.value = newScope;
}
</script>

<script lang="ts">
import { VIEW_GROUP } from '@/views/ViewGroup';

export default {
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/loggers',
      parent: 'instances',
      path: 'loggers',
      label: 'instances.loggers.label',
      component: this,
      group: VIEW_GROUP.LOGGING,
      order: 300,
      isEnabled: ({ instance }) => instance.hasEndpoint('loggers'),
    });
  },
};
</script>
