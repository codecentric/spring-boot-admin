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
            'bg-gray-50':
              index % 2 === 0 || showDetails[conditionalBean.name] === true,
            'px-3 pt-2': showDetails[conditionalBean.name] === true,
            'px-4 py-3': showDetails[conditionalBean.name] !== true,
          }"
        >
          <div class="flex-1 sm:break-all">
            <h4
              :class="{
                'font-bold': showDetails[conditionalBean.name] === true,
              }"
              :title="conditionalBean.name"
              @click="toggle(conditionalBean.name)"
              v-text="conditionalBean.name"
            />
            <template v-if="showDetails[conditionalBean.name] === true">
              <h5
                v-if="
                  conditionalBean.matched.length &&
                  conditionalBean.notMatched.length
                "
                class="px-2 py-1"
              >
                {{ $t('instances.conditions.matched') }}
              </h5>
              <template
                v-for="matchedCondition in conditionalBean.matched"
                :key="matchedCondition.condition"
              >
                <div class="py-2 m-1 border rounded">
                  <conditions-list-details :condition="matchedCondition" />
                </div>
              </template>
              <h5 v-if="conditionalBean.notMatched.length" class="px-2 py-1">
                {{ $t('instances.conditions.not-matched') }}
              </h5>
              <template
                v-for="notMatchedCondition in conditionalBean.notMatched"
                :key="notMatchedCondition.condition"
              >
                <div class="py-2 m-1 border rounded">
                  <conditions-list-details :condition="notMatchedCondition" />
                </div>
              </template>
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
