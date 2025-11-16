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
            'px-4 py-3': true,
          }"
        >
          <div class="flex-1 sm:break-all">
            <component
              :is="hasEntries(conditionalBean) ? 'button' : 'div'"
              v-sticks-below="'#subnavigation'"
              :class="{
                'pb-2': showDetails[conditionalBean.name] === true,
                'cursor-pointer': hasEntries(conditionalBean),
              }"
              :title="conditionalBean.name"
              @click="toggle(conditionalBean.name)"
            >
              <font-awesome-icon
                v-if="hasEntries(conditionalBean)"
                :icon="faChevronRight"
                class="transition-[transform] rotate-0"
                :class="{
                  'rotate-90': showDetails[conditionalBean.name],
                }"
              />
              {{ conditionalBean.name }}
            </component>
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

<script lang="ts">
import { faChevronRight } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { PropType } from 'vue';

import { ConditionalBean } from '@/views/instances/beans/ConditionalBean';
import ConditionsListDetails from '@/views/instances/conditions/conditions-list-details.vue';

export default {
  components: { FontAwesomeIcon, ConditionsListDetails },
  props: {
    conditionalBeans: {
      type: Array as PropType<ConditionalBean[]>,
      default: () => [],
    },
  },
  data() {
    return {
      faChevronRight,
      showDetails: {},
    };
  },
  methods: {
    hasEntries(conditionalBean: ConditionalBean) {
      return (
        conditionalBean.notMatched?.length > 0 ||
        conditionalBean.matched?.length > 0
      );
    },
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
