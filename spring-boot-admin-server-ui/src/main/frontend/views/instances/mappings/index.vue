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
  <section class="section" :class="{ 'is-loading' : !hasLoaded }">
    <template v-if="hasLoaded">
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
            <span v-text="$t('instances.mappings.fetch_failed')" />
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <div v-if="isOldMetrics" class="message is-warning">
        <div class="message-body" v-text="$t('instances.mappings.mappings_not_supported_spring_boot_1')" />
      </div>
      <template v-for="(context, ctxName) in contexts">
        <h3 class="title" v-text="ctxName" :key="ctxName" />

        <dispatcher-mappings v-if="!isEmpty(context.mappings.dispatcherServlets)"
                             :key="`${ctxName}_dispatcherServlets`"
                             :dispatchers="context.mappings.dispatcherServlets"
        />

        <dispatcher-mappings v-if="!isEmpty(context.mappings.dispatcherHandlers)"
                             :key="`${ctxName}_dispatcherHandlers`"
                             :dispatchers="context.mappings.dispatcherHandlers"
        />

        <servlet-mappings :key="`${ctxName}_servlets`"
                          :servlets="context.mappings.servlets"
        />

        <servlet-filter-mappings :key="`${ctxName}_servletFilters`"
                                 :servlet-filters="context.mappings.servletFilters"
        />
      </template>
    </template>
  </section>
</template>

<script>
  import Instance from '@/services/instance';
  import DispatcherMappings from '@/views/instances/mappings/DispatcherMappings';
  import ServletFilterMappings from '@/views/instances/mappings/ServletFilterMappings';
  import ServletMappings from '@/views/instances/mappings/ServletMappings';
  import isEmpty from 'lodash/isEmpty';
  import {VIEW_GROUP} from '../../index';

  export default {
    components: {DispatcherMappings, ServletMappings, ServletFilterMappings},
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    data: () => ({
      hasLoaded: false,
      error: null,
      contexts: null,
      isOldMetrics: false
    }),
    created() {
      this.fetchMappings();
    },
    computed: {},
    methods: {
      isEmpty,
      async fetchMappings() {
        this.error = null;
        try {
          const res = await this.instance.fetchMappings();
          if (res.headers['content-type'].includes('application/vnd.spring-boot.actuator.v2')) {
            this.contexts = res.data.contexts;
          } else {
            this.isOldMetrics = true;
          }
        } catch (error) {
          console.warn('Fetching mappings failed:', error);
          this.error = error;
        }
        this.hasLoaded = true;
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/mappings',
        parent: 'instances',
        path: 'mappings',
        label: 'instances.mappings.label',
        group: VIEW_GROUP.WEB,
        component: this,
        order: 450,
        isEnabled: ({instance}) => instance.hasEndpoint('mappings')
      });
    }
  }
</script>
