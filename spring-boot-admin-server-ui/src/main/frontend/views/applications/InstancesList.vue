<!--
  - Copyright 2014-2024 the original author or authors.
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
  <ul>
    <li
      v-for="instance in instances"
      :key="instance.id"
      :data-testid="instance.id"
      class="flex p-2 pr-4 hover:bg-gray-100 gap-2 odd:bg-gray-50 items-center"
      @click.stop="showDetails(instance)"
    >
      <div class="pt-1 md:w-16 text-center">
        <sba-status
          :date="instance.statusTimestamp"
          :status="instance.statusInfo.status"
        />
      </div>
      <div class="flex-1">
        <div
          class="flex gap-2 items-center instance-item"
          :class="{ 'instance--show-url': instance.showUrl() }"
        >
          <ItemInformation
            class="instance-item-information"
            :instance="instance"
          />
          <div class="hidden lg:block">
            <slot :instance="instance" name="actions" />
          </div>
        </div>
        <ItemTags :instance="instance" />
      </div>
    </li>
  </ul>
</template>

<script lang="ts" setup>
import { PropType } from 'vue';
import { useRouter } from 'vue-router';

import SbaStatus from '@/components/sba-status.vue';

import Instance from '@/services/instance';
import ItemInformation from '@/views/applications/listItem/ItemInformation.vue';
import ItemTags from '@/views/applications/listItem/ItemTags.vue';

const router = useRouter();

defineProps({
  instances: {
    type: Object as PropType<Array<Instance>>,
    default: () => [] as Instance[],
  },
  showNotificationSettings: {
    type: Boolean,
    default: false,
  },
  hasActiveNotificationFilter: {
    type: Function,
    default: () => false,
  },
});

const showDetails = (instance: Instance) => {
  router.push({
    name: 'instances/details',
    params: { instanceId: instance.id },
  });
};
</script>

<style scoped>
.instance--show-url {
  .instance-item-information {
    @apply gap-1;
    grid-area: 1 / 1 / 1 / 3;
  }
}
</style>

<style>
.instance-item.instance--show-url {
  .instance-id {
    @apply text-xs font-light;
  }
  .instance-version {
    @apply text-xs font-light;
  }
}
</style>
