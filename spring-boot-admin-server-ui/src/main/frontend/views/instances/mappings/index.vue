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
  <sba-instance-section :error="error" :loading="!hasLoaded">
    <div v-if="isOldMetrics" class="message is-warning">
      <div
        class="message-body"
        v-text="$t('instances.mappings.mappings_not_supported_spring_boot_1')"
      />
    </div>
    <template v-for="(context, ctxName) in contexts" :key="ctxName">
      <sba-panel :seamless="true" :title="ctxName">
        <dispatcher-mappings
          v-if="hasDispatcherServlets(context)"
          :key="`${ctxName}_dispatcherServlets`"
          :dispatchers="context.mappings.dispatcherServlets"
        />

        <dispatcher-mappings
          v-if="hasDispatcherHandlers(context)"
          :key="`${ctxName}_dispatcherHandlers`"
          :dispatchers="context.mappings.dispatcherHandlers"
        />

        <servlet-mappings
          v-if="hasServlet(context)"
          :key="`${ctxName}_servlets`"
          :servlets="context.mappings.servlets"
        />

        <servlet-filter-mappings
          v-if="hasServletFilters(context)"
          :key="`${ctxName}_servletFilters`"
          :servlet-filters="context.mappings.servletFilters"
        />
      </sba-panel>
    </template>
  </sba-instance-section>
</template>

<script>
import SbaPanel from '@/components/sba-panel';

import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import DispatcherMappings from '@/views/instances/mappings/DispatcherMappings';
import ServletFilterMappings from '@/views/instances/mappings/ServletFilterMappings';
import ServletMappings from '@/views/instances/mappings/ServletMappings';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

export default {
  components: {
    SbaPanel,
    SbaInstanceSection,
    DispatcherMappings,
    ServletMappings,
    ServletFilterMappings,
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
    contexts: null,
    isOldMetrics: false,
  }),
  created() {
    this.fetchMappings();
  },
  methods: {
    hasDispatcherServlets(context) {
      return context?.mappings?.dispatcherServlets !== undefined;
    },
    hasDispatcherHandlers(context) {
      return context?.mappings?.dispatcherHandlers !== undefined;
    },
    hasServlet(context) {
      return context?.mappings?.servlets !== undefined;
    },
    hasServletFilters(context) {
      return context?.mappings?.servletFilters !== undefined;
    },
    async fetchMappings() {
      this.error = null;
      try {
        const res = await this.instance.fetchMappings();
        const supportedContentTypes = [
          'application/vnd.spring-boot.actuator.v3+json',
          'application/vnd.spring-boot.actuator.v2+json',
        ];
        if (supportedContentTypes.includes(res.headers['content-type'])) {
          this.contexts = res.data.contexts;
        } else {
          this.isOldMetrics = true;
        }
      } catch (error) {
        console.warn('Fetching mappings failed:', error);
        this.error = error;
      }
      this.hasLoaded = true;
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/mappings',
      parent: 'instances',
      path: 'mappings',
      label: 'instances.mappings.label',
      group: VIEW_GROUP.WEB,
      component: this,
      order: 450,
      isEnabled: ({ instance }) => instance.hasEndpoint('mappings'),
    });
  },
};
</script>
