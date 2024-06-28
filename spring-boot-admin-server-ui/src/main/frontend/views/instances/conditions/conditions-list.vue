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
  <div class="-mx-4 -my-3">
    <template
      v-for="(conditionalBean, index) in conditionalBeans"
      :key="conditionalBean.name"
    >
      <div class="m-1 border rounded shadow-sm">
        <div
          :key="conditionalBean.name"
          class="flex items-center"
          :class="{
            'bg-gray-50': index % 2 === 0,
            'px-3 py-2': true,
          }"
        >
          <div class="flex-1 sm:break-all">
            <div
              class="font-bold"
              :title="conditionalBean.name"
              v-text="conditionalBean.name"
            />
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
export default {
  props: {
    conditionalBeans: {
      type: Array,
      default: () => [],
    },
  },
};
</script>
