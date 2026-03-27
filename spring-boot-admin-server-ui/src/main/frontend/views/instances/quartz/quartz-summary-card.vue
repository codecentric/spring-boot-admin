<!--
  Copyright 2014-2024 the original author or authors.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<template>
  <div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
    <div class="flex items-center justify-between">
      <div>
        <p class="text-sm font-medium uppercase tracking-wide text-gray-600">
          {{ label }}
        </p>
        <p class="mt-2 text-3xl font-bold text-gray-900">{{ value }}</p>
      </div>
      <font-awesome-icon :icon="icon" class="h-10 w-10 text-gray-400" />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  label: string;
  value: number;
  icon: string;
}

defineProps<Props>();
</script>
