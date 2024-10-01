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
  <table>
    <colgroup>
      <col class="text-gray-500" />
      <col class="py-3" />
    </colgroup>
    <tbody>
      <!-- Condition -->
      <tr v-if="condition.condition">
        <th
          :id="`conditions-label-${id}`"
          class="label"
          v-text="$t('instances.conditions.condition')"
        />
        <td
          :aria-labelledby="`conditions-label-${id}`"
          class="value"
          v-text="condition.condition"
        />
      </tr>

      <!-- Message -->
      <tr v-if="condition.message">
        <th
          :id="`message-label-${id}`"
          class="label"
          v-text="$t('instances.conditions.message')"
        />
        <td
          :aria-labelledby="`message-label-${id}`"
          class="value"
          v-text="condition.message"
        />
      </tr>
    </tbody>
  </table>
</template>

<script setup lang="ts">
defineProps<{
  condition: {
    condition: string;
    message: string;
  };
}>();
const id = Math.floor(Math.random() * 1_000_000);
</script>

<style>
.label {
  @apply text-gray-500 text-sm text-right px-2 align-top w-32;
}
.value {
  @apply text-sm break-all;
}
</style>
