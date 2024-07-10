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
  <sba-instance-section :error="error" :loading="!hasLoaded">
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
        </sba-input>
      </sba-sticky-subnav>
    </template>

    <tree-graph
      v-for="sbomId in sboms"
      :key="sbomId"
      :instance="instance"
      :sbom-id="sbomId"
      :filter="filter"
    ></tree-graph>
  </sba-instance-section>
</template>

<script>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

import SbaInput from '@/components/sba-input';
import SbaStickySubnav from '@/components/sba-sticky-subnav.vue';

import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import TreeGraph from '@/views/instances/sbomdependencytrees/tree.vue';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

export default {
  components: {
    TreeGraph,
    FontAwesomeIcon,
    SbaStickySubnav,
    SbaInstanceSection,
    SbaInput,
  },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    contexts: [],
    sboms: [],
    filter: '',
  }),
  computed: {},
  created() {
    this.fetchSboms();
  },
  methods: {
    async fetchSboms() {
      this.error = null;
      try {
        const res = await this.instance.fetchSbomIds();
        this.sboms = res.data.ids;
      } catch (error) {
        console.warn('Fetching sbom ids failed:', error);
        this.error = error;
      }
      this.hasLoaded = true;
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/sbom',
      parent: 'instances',
      path: 'sbom',
      label: 'instances.sbom.label',
      group: VIEW_GROUP.INSIGHTS,
      component: this,
      order: 120,
      isEnabled: ({ instance }) => instance.hasEndpoint('sbom'),
    });
  },
};
</script>
