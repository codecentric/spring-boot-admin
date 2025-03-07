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
  <div class="flex">
    <sba-button
      v-if="instanceCount <= 1 || modelValue === ActionScope.APPLICATION"
      :class="classNames"
      :title="$t('term.affects_all_instances', { count: instanceCount })"
      class="w-full"
      size="sm"
      @click="() => (modelValue = ActionScope.INSTANCE)"
    >
      <span v-text="$t('term.application')" />
    </sba-button>
    <sba-button
      v-else
      :class="classNames"
      :title="$t('term.affects_this_instance_only')"
      class="w-full"
      size="sm"
      @click="() => (modelValue = ActionScope.APPLICATION)"
    >
      <span v-text="$t('term.instance')" />
    </sba-button>

    <p v-if="showInfo" class="text-center text-xs pt-1 truncate">
      <span
        v-if="modelValue === ActionScope.APPLICATION"
        v-text="$t('term.affects_all_instances', { count: instanceCount })"
      />
      <span v-else v-text="$t('term.affects_this_instance_only')" />
    </p>
  </div>
</template>

<script lang="ts" setup>
import { ActionScope } from '@/components/ActionScope';
import SbaButton from '@/components/sba-button.vue';

const modelValue = defineModel({
  type: String,
  default: ActionScope.APPLICATION,
});

const { instanceCount, showInfo = true } = defineProps<{
  instanceCount: number;
  showInfo?: boolean;
  classNames?: string | string[] | Record<string, boolean>;
}>();
</script>
