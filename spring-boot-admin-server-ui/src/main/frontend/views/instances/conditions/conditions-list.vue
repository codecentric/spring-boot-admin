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
            'px-3 pt-2': true,
          }"
          @click="toggle(conditionalBean.name)"
        >
          <div class="flex-1 sm:break-all">
            <div
              class="font-bold"
              :title="conditionalBean.name"
              v-text="conditionalBean.name"
            />
            <template
              v-for="matchedCondition in conditionalBean.matched"
              :key="matchedCondition.condition"
            >
              <div
                v-if="showDetails[conditionalBean.name] === true"
                class="py-2 m-1 border rounded"
              >
                <conditions-list-details :condition="matchedCondition" />
              </div>
            </template>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import ConditionsListDetails from '@/views/instances/conditions/conditions-list-details.vue';

export default {
  components: { ConditionsListDetails },
  props: {
    conditionalBeans: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      showDetails: {},
    };
  },
  methods: {
    toggle(name) {
      if (this.showDetails[name]) {
        this.showDetails = {
          ...this.showDetails,
          [name]: null,
        };
      } else {
        this.showDetails = {
          ...this.showDetails,
          [name]: true,
        };
      }
    },
  },
};
</script>
