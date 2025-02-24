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
  <sba-instance-section
    :error="error"
    :layout-options="{ noMargin: true, isFlex: true }"
    :loading="!hasLoaded"
    class="bg-white h-full"
  >
    <nav class="w-1/4 p-2">
      <div class="nav">
        <button
          v-for="domain in domains"
          :key="domain.domain"
          :class="{ 'is-active': domain === selectedDomain }"
          class="nav-item"
          @click="select(domain)"
          v-text="domain.domain"
        />
      </div>
    </nav>
    <div class="flex-1 overflow-scroll bg-white p-2">
      <div class="flex h-full items-start">
        <div
          v-if="selectedDomain"
          :title="selectedDomain.domain"
          class="flex-1 gap-1 grid grid-cols-1"
        >
          <div
            v-for="mBean in selectedDomain.mBeans"
            :id="mBean.descriptor.raw"
            :key="mBean.descriptor.raw"
            class="m-bean"
          >
            <header
              :class="{
                'is-primary': mBean === selectedMBean,
                'is-selectable': mBean !== selectedMBean,
              }"
              class="m-bean--header hero"
              @click="select(selectedDomain, mBean)"
            >
              <sba-icon-button
                v-if="mBean === selectedMBean"
                :icon="['far', 'times-circle']"
                class="m-bean--header--close"
                @click.stop="select(selectedDomain)"
              />
              <dl class="m-bean-attributes">
                <div
                  v-for="attribute in mBean.descriptor.attributes"
                  :key="`mBean-desc-${attribute.name}`"
                >
                  <dt v-text="attribute.name" />
                  <dd v-text="attribute.value" />
                </div>
              </dl>
            </header>
            <div class="relative">
              <div v-if="mBean === selectedMBean" class="tabs">
                <ul class="nav-tabs">
                  <li v-if="mBean.attr">
                    <a
                      :class="{ 'is-active': selected.view === 'attributes' }"
                      class="nav-item"
                      @click.stop="
                        select(selectedDomain, selectedMBean, 'attributes')
                      "
                      v-text="$t('term.attributes')"
                    />
                  </li>
                  <li v-if="mBean.op">
                    <a
                      :class="{ 'is-active': selected.view === 'operations' }"
                      class="nav-item"
                      @click.stop="
                        select(selectedDomain, selectedMBean, 'operations')
                      "
                      v-text="$t('term.operations')"
                    />
                  </li>
                </ul>
              </div>

              <div v-if="mBean === selectedMBean" class="mt-3 mx-1">
                <m-bean-attributes
                  v-if="selected.view === 'attributes'"
                  :application="application"
                  :domain="selectedDomain.domain"
                  :instance="instance"
                  :m-bean="mBean"
                />
                <m-bean-operations
                  v-if="selected.view === 'operations'"
                  :application="application"
                  :domain="selectedDomain.domain"
                  :instance="instance"
                  :m-bean="mBean"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </sba-instance-section>
</template>

<script>
import { isEmpty, sortBy } from 'lodash-es';
import { directive as onClickaway } from 'vue3-click-away';

import Application from '@/services/application';
import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import { MBean } from '@/views/instances/jolokia/MBean';
import mBeanAttributes from '@/views/instances/jolokia/m-bean-attributes';
import mBeanOperations from '@/views/instances/jolokia/m-bean-operations';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

export default {
  components: {
    SbaInstanceSection,
    mBeanOperations,
    mBeanAttributes,
  },
  directives: { onClickaway },
  props: {
    application: {
      type: Application,
      required: true,
    },
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    domains: [],
    selected: {
      domain: null,
      mBean: null,
      view: null,
    },
  }),
  computed: {
    selectedDomain() {
      return this.domains.find((d) => d.domain === this.selected.domain);
    },
    selectedMBean() {
      return (
        this.selectedDomain &&
        this.selectedDomain.mBeans.find(
          (b) => b.descriptor.raw === this.selected.mBean,
        )
      );
    },
  },
  watch: {
    $route: {
      immediate: true,
      handler() {
        if (this.$route.name === 'instances/jolokia') {
          if (!isEmpty(this.$route.query)) {
            this.selected = this.$route.query;
          } else if (this.domains.length > 0) {
            this.select(this.domains[0]);
          }
        }
      },
    },
    async selectedMBean(newVal) {
      if (newVal) {
        if (document.getElementById(newVal.descriptor.raw)?.scrollIntoView) {
          document.getElementById(newVal.descriptor.raw)?.scrollIntoView();
        }
      }
    },
  },
  created() {
    this.fetchMBeans();
  },
  methods: {
    async fetchMBeans() {
      this.error = null;
      try {
        const res = await this.instance.listMBeans();
        const domains = sortBy(res.data, [(d) => d.domain]);
        this.domains = domains.map((domain) => ({
          ...domain,
          mBeans: sortBy(
            domain.mBeans.map((mBean) => new MBean(mBean)),
            [(b) => b.descriptor.displayName],
          ),
        }));
        if (!this.selectedDomain && this.domains.length > 0) {
          this.select(this.domains[0]);
        }
      } catch (error) {
        console.warn('Fetching MBeans failed:', error);
        this.error = error;
      }
      this.hasLoaded = true;
    },
    select(domain, mBean, view) {
      const selected = {
        domain: domain && domain.domain,
        mBean: mBean && mBean.descriptor.raw,
        view:
          view ||
          (mBean
            ? mBean.attr
              ? 'attributes'
              : mBean.op
                ? 'operations'
                : null
            : null),
      };
      this.$router.replace({
        name: 'instances/jolokia',
        query: selected,
      });
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/jolokia',
      parent: 'instances',
      path: 'jolokia',
      label: 'instances.jolokia.label',
      component: this,
      group: VIEW_GROUP.JVM,
      order: 350,
      isEnabled: ({ instance }) => instance.hasEndpoint('jolokia'),
    });
  },
};
</script>

<style lang="scss" scoped>
.tabs {
  @apply text-sm font-medium text-center text-gray-500 border-b border-gray-200 dark:text-gray-400 dark:border-gray-700;

  .nav-tabs {
    @apply flex flex-wrap -mb-px;

    .nav-item {
      @apply cursor-pointer inline-block p-4 border-b-2 border-transparent rounded-t-lg hover:text-gray-600 hover:border-gray-300 dark:hover:text-gray-300;

      &.is-active {
        @apply inline-block p-4 text-blue-600 border-b-2 border-blue-600 rounded-t-lg dark:text-blue-500 dark:border-blue-500;
      }
    }
  }
}

.m-bean {
  transition: all ease-out 86ms;
  @apply relative p-2 rounded border;
  @apply bg-white;
}

.m-bean:hover {
}

.m-bean.is-active {
  margin: 0.75rem -0.75rem;
  max-width: unset;
}

.m-bean.is-active .m-bean--header {
  padding-bottom: 0;
}

.m-bean--header {
  cursor: pointer;
}

.m-bean--header .level .level-left {
  width: 100%;
}

.m-bean--header .level .level-left .level-item {
  min-width: 0;
  flex-shrink: 1;
}

.m-bean--header .level .level-left .level-item p {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.m-bean--header--close {
  position: absolute;
  right: 0;
  top: 0;
}

.m-bean-attributes {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;

  dt {
    @apply text-xs text-gray-500;
  }
}

.nav {
  @apply flex flex-col;

  .nav-item {
    @apply px-2 py-1 rounded w-full text-left;

    &:nth-child(odd) {
      @apply bg-gray-50;
    }

    &.is-active {
      @apply font-bold;
    }
  }
}
</style>
