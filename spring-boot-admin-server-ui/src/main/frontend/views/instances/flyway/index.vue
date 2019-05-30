<!--
  - Copyright 2014-2019 the original author or authors.
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
            <span v-text="$t('instances.flyway.fetch_failed')" />
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <template v-for="(context, ctxName) in contexts">
        <h3 class="title" v-text="ctxName" :key="ctxName" />
        <sba-panel v-for="(report, name) in context.flywayBeans" :key="`${ctxName}-${name}`" :title="name"
                   :header-sticks-below="['#navigation']"
                   class="migration"
        >
          <table class="table is-fullwidth">
            <thead>
              <tr>
                <th v-text="$t('instances.flyway.type')" />
                <th v-text="$t('instances.flyway.checksum')" />
                <th v-text="$t('instances.flyway.version')" />
                <th v-text="$t('instances.flyway.description')" />
                <th v-text="$t('instances.flyway.script')" />
                <th v-text="$t('instances.flyway.state')" />
                <th v-text="$t('instances.flyway.installed_by')" />
                <th v-text="$t('instances.flyway.installed_on')" />
                <th v-text="$t('instances.flyway.installed_rank')" />
                <th v-text="$t('instances.flyway.execution_time')" />
              </tr>
            </thead>
            <tbody>
              <tr v-for="migration in report.migrations" :key="migration.checksum">
                <td v-text="migration.type" />
                <td v-text="migration.checksum" />
                <td v-text="migration.version" />
                <td class="is-breakable" v-text="migration.description" />
                <td class="is-breakable" v-text="migration.script" />
                <td>
                  <span v-text="migration.state" class="tag"
                        :class="stateClass(migration.state)"
                  />
                </td>
                <td v-text="migration.installedBy" />
                <td v-text="migration.installedOn" />
                <td v-text="migration.installedRank" />
                <td v-text="`${migration.executionTime}ms`" />
              </tr>
            </tbody>
          </table>
        </sba-panel>
      </template>
    </template>
  </section>
</template>

<script>
  import Instance from '@/services/instance';
  import {VIEW_GROUP} from '../../index';

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    data: () => ({
      hasLoaded: false,
      error: null,
      contexts: null
    }),
    computed: {},
    created() {
      this.fetchFlyway();
    },
    methods: {
      async fetchFlyway() {
        this.error = null;
        try {
          const res = await this.instance.fetchFlyway();
          this.contexts = res.data.contexts;
        } catch (error) {
          console.warn('Fetching flyway reports failed:', error);
          this.error = error;
        }
        this.hasLoaded = true;
      },
      stateClass(state) {
        switch (state) {
          case 'BASELINE' :
          case  'MISSING_SUCCESS' :
          case  'SUCCESS' :
          case  'OUT_OF_ORDER' :
          case  'FUTURE_SUCCESS' :
            return 'is-success';
          case 'PENDING':
          case 'ABOVE_TARGET':
          case 'PREINIT':
          case 'BELOW_BASELINE':
          case 'IGNORED':
            return 'is-warning';
          case 'MISSING_FAILED':
          case 'FAILED':
          case 'FUTURE_FAILED':
            return 'is-danger';
          default:
            return 'is-light';
        }
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/flyway',
        parent: 'instances',
        path: 'flyway',
        component: this,
        label: 'instances.flyway.label',
        group: VIEW_GROUP.DATA,
        order: 900,
        isEnabled: ({instance}) => instance.hasEndpoint('flyway')
      });
    }
  }
</script>
