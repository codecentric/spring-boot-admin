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
  <sba-instance-section :error="error" :loading="isLoading">
    <template #before>
      <sba-sticky-subnav>
        <sba-input
          v-model="filter"
          :placeholder="$t('term.filter')"
          name="filter"
          type="search"
        >
          <template #prepend>
            <font-awesome-icon icon="filter" />
          </template>
          <template #append>
            {{ filterResultString }}
          </template>
        </sba-input>
      </sba-sticky-subnav>
    </template>

    <template v-for="context in filteredContexts" :key="context.name">
      <sba-panel
        :id="`${context.name}-context`"
        :header-sticks-below="'#subnavigation'"
        :title="context.name"
      >
        <sba-panel
          v-if="context.unconditionalClasses.length"
          :title="$t('instances.conditions.unconditionalClasses')"
        >
          <conditions-list
            :key="`${context.name}-positiveMatches`"
            :conditional-beans="context.unconditionalClasses"
          />
        </sba-panel>

        <sba-panel
          v-if="context.positiveMatches.length"
          :title="$t('instances.conditions.positive-matches')"
        >
          <conditions-list
            :key="`${context.name}-positiveMatches`"
            :conditional-beans="context.positiveMatches"
          />
        </sba-panel>
        <sba-panel
          v-if="context.negativeMatches.length"
          :title="$t('instances.conditions.negative-matches')"
        >
          <conditions-list
            :key="`${context.name}-negativeMatches`"
            :conditional-beans="context.negativeMatches"
          />
        </sba-panel>
      </sba-panel>
    </template>
  </sba-instance-section>
</template>

<script lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { isEmpty } from 'lodash-es';

import SbaStickySubnav from '@/components/sba-sticky-subnav.vue';

import Instance from '@/services/instance';
import { compareBy } from '@/utils/collections';
import { VIEW_GROUP } from '@/views/ViewGroup';
import { ConditionalBean } from '@/views/instances/beans/ConditionalBean';
import ConditionsList from '@/views/instances/conditions/conditions-list.vue';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

const mapPositiveMatches = (positiveMatches) => {
  return Object.keys(positiveMatches).map((matchedBeanName) => {
    return {
      name: matchedBeanName,
      matched: positiveMatches[matchedBeanName],
      notMatched: [],
    } as ConditionalBean;
  });
};

const mapNegativeMatches = (negativeMatches) => {
  return Object.keys(negativeMatches).map((matchedBeanName) => ({
    name: matchedBeanName,
    matched: negativeMatches[matchedBeanName].matched,
    notMatched: negativeMatches[matchedBeanName].notMatched,
  }));
};

const mapUnconditionalClasses = (unconditionalClasses) => {
  return (unconditionalClasses || []).map((bean) => ({ name: bean }));
};

const mapContexts = (conditionsData) => {
  if (isEmpty(conditionsData.contexts)) {
    return [];
  }
  return Object.keys(conditionsData.contexts).map((contextName) => ({
    positiveMatches: mapPositiveMatches(
      conditionsData.contexts[contextName].positiveMatches,
    ),
    negativeMatches: mapNegativeMatches(
      conditionsData.contexts[contextName].negativeMatches,
    ),
    unconditionalClasses: mapUnconditionalClasses(
      conditionsData.contexts[contextName].unconditionalClasses,
    ),
    name: contextName,
    parent: conditionsData.contexts[contextName].parentId,
  }));
};

export default {
  components: {
    FontAwesomeIcon,
    SbaStickySubnav,
    ConditionsList,
    SbaInstanceSection,
  },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    isLoading: false,
    error: null,
    contexts: [],
    filter: '',
  }),
  computed: {
    filterResultString() {
      const totalMatches = this.contexts.reduce((count, ctx) => {
        return (
          count + ctx.positiveMatches?.length + ctx.negativeMatches?.length
        );
      }, 0);

      const filteredMatches = this.filteredContexts.reduce((count, ctx) => {
        return (
          count + ctx.positiveMatches?.length + ctx.negativeMatches?.length
        );
      }, 0);

      return `${filteredMatches}/${totalMatches}`;
    },
    filteredContexts() {
      const filterFn = this.getFilterFn();
      return this.contexts.map((ctx) => ({
        ...ctx,
        positiveMatches: ctx.positiveMatches
          .filter(filterFn)
          .sort(compareBy((conditionalBean) => conditionalBean.name)),
        negativeMatches: ctx.negativeMatches
          .filter(filterFn)
          .sort(compareBy((conditionalBean) => conditionalBean.name)),
      }));
    },
  },
  created() {
    this.fetchConditions();
  },
  methods: {
    getFilterFn() {
      if (!this.filter) {
        return () => true;
      }
      const regex = new RegExp(this.filter, 'i');
      return (conditionalBean) => conditionalBean.name.match(regex);
    },
    async fetchConditions() {
      this.error = null;
      this.isLoading = true;
      try {
        const res = await this.instance.fetchConditions();
        this.contexts = mapContexts(res.data);
      } catch (error) {
        console.warn('Fetching conditions failed:', error);
        this.error = error;
      }
      this.isLoading = false;
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/conditions',
      parent: 'instances',
      path: 'conditions',
      label: 'instances.conditions.label',
      group: VIEW_GROUP.INSIGHTS,
      component: this,
      order: 120,
      isEnabled: ({ instance }) => instance.hasEndpoint('conditions'),
    });
  },
};
</script>
