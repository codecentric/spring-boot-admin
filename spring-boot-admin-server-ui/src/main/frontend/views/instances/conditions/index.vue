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

    <template v-for="context in contexts" :key="context.name">
      <sba-panel :header-sticks-below="'#subnavigation'" :title="context.name">
        <div>{{ context.positiveMatches }}</div>
        <div>{{ context.negativeMatches }}</div>
      </sba-panel>
    </template>
  </sba-instance-section>
</template>

<script>
import { isEmpty } from 'lodash-es';

import Instance from '@/services/instance';
import { compareBy } from '@/utils/collections';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

class ConditionalBean {
  constructor(name, positiveMatches, negativeMatches) {
    this.name = name;
    this.matched = positiveMatches.map((condition) => new Condition(condition));
    this.notMatched = negativeMatches.map(
      (condition) => new Condition(condition),
    );
  }
}

class Condition {
  constructor(condition) {
    this.condition = condition.condition;
    this.message = condition.message;
  }
}

const mapPositiveMatches = (positiveMatches) => {
  return Object.keys(positiveMatches).map(
    (matchedBeanName) =>
      new ConditionalBean(
        matchedBeanName,
        positiveMatches[matchedBeanName],
        [],
      ),
  );
};

const mapNegativeMatches = (negativeMatches) => {
  return Object.keys(negativeMatches).map(
    (matchedBeanName) =>
      new ConditionalBean(
        matchedBeanName,
        negativeMatches[matchedBeanName].matched,
        negativeMatches[matchedBeanName].notMatched,
      ),
  );
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
    name: contextName,
    parent: conditionsData.contexts[contextName].parentId,
  }));
};

export default {
  components: { SbaInstanceSection },
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
      const totalBeans = this.contexts.reduce((count, ctx) => {
        return count + ctx.beans?.length;
      }, 0);
      const filteredBeansLength = this.filteredContexts.reduce((count, ctx) => {
        return count + ctx.beans?.length;
      }, 0);

      return `${filteredBeansLength}/${totalBeans}`;
    },
    filteredContexts() {
      const filterFn = this.getFilterFn();
      return this.contexts.map((ctx) => ({
        ...ctx,
        beans: ctx.beans.filter(filterFn).sort(compareBy((bean) => bean.name)),
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
      return (bean) =>
        bean.name.match(regex) ||
        (bean.aliases && bean.aliases.some((alias) => alias.match(regex)));
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
