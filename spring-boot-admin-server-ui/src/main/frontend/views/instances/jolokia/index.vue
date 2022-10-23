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
  <sba-instance-section :error="error" :loading="!hasLoaded">
    <div class="flex">
      <div class="flex-1">
        <sba-panel
          v-on-clickaway="
            () => mBean === selectedMBean && select(selectedDomain)
          "
          :title="selectedDomain.domain"
        >
          <div
            v-for="mBean in selectedDomain.mBeans"
            :id="mBean.descriptor.raw"
            :key="mBean.descriptor.raw"
          >
            <header
              :class="{
                'is-primary': mBean === selectedMBean,
                'is-selectable': mBean !== selectedMBean,
              }"
              class="m-bean--header hero"
              @click="select(selectedDomain, mBean)"
            >
              <div class="level is-clipped">
                <div class="level-left">
                  <div
                    v-for="attribute in mBean.descriptor.attributes"
                    :key="`mBean-desc-${attribute.name}`"
                    class="level-item is-narrow"
                  >
                    <div
                      :title="`${attribute.name} ${attribute.value}`"
                      class="is-clipped"
                    >
                      <p class="heading" v-text="attribute.name" />
                      <p class="title is-size-6" v-text="attribute.value" />
                    </div>
                  </div>
                </div>
              </div>
              <sba-icon-button
                v-if="mBean === selectedMBean"
                :icon="['far', 'times-circle']"
                class="m-bean--header--close has-text-white"
                @click.stop="select(selectedDomain)"
              />
              <div
                v-if="mBean === selectedMBean"
                class="hero-foot tabs is-boxed"
              >
                <ul>
                  <li
                    v-if="mBean.attr"
                    :class="{ 'is-active': selected.view === 'attributes' }"
                  >
                    <a
                      @click.stop="
                        select(selectedDomain, selectedMBean, 'attributes')
                      "
                      v-text="$t('term.attributes')"
                    />
                  </li>
                  <li
                    v-if="mBean.op"
                    :class="{ 'is-active': selected.view === 'operations' }"
                  >
                    <a
                      @click.stop="
                        select(selectedDomain, selectedMBean, 'operations')
                      "
                      v-text="$t('term.operations')"
                    />
                  </li>
                </ul>
              </div>
            </header>

            <div v-if="mBean === selectedMBean" class="card-content">
              <m-bean-attributes
                v-if="selected.view === 'attributes'"
                :domain="selectedDomain.domain"
                :instance="instance"
                :m-bean="mBean"
              />
              <m-bean-operations
                v-if="selected.view === 'operations'"
                :domain="selectedDomain.domain"
                :instance="instance"
                :m-bean="mBean"
              />
            </div>
          </div>
        </sba-panel>
      </div>

      <div class="w-80 truncate">
        <nav>
          <p class="menu-label" v-text="$t('instances.jolokia.domains')" />
          <ul class="list-disc">
            <li v-for="domain in domains" :key="domain.domain">
              <a
                :class="{ 'is-active': domain === selectedDomain }"
                class=""
                @click="select(domain)"
                v-text="domain.domain"
              />
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </sba-instance-section>
</template>

<script>
import { isEmpty, sortBy } from 'lodash-es';
import { directive as onClickaway } from 'vue3-click-away';

import Instance from '@/services/instance';
import { MBean } from '@/views/instances/jolokia/MBean';
import mBeanAttributes from '@/views/instances/jolokia/m-bean-attributes';
import mBeanOperations from '@/views/instances/jolokia/m-bean-operations';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

export default {
  components: { SbaInstanceSection, mBeanOperations, mBeanAttributes },
  directives: { onClickaway },
  props: {
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
          (b) => b.descriptor.raw === this.selected.mBean
        )
      );
    },
  },
  watch: {
    $route: {
      immediate: true,
      handler() {
        if (!isEmpty(this.$route.query)) {
          this.selected = this.$route.query;
        } else if (this.domains.length > 0) {
          this.select(this.domains[0]);
        }
      },
    },
    async selectedMBean(newVal) {
      if (newVal) {
        await this.$nextTick();
        const el = document.getElementById(newVal.descriptor.raw);
        if (el) {
          const scrollingEl = document.querySelector('main');
          const navigation = document.querySelector('#navigation');
          const navbarOffset =
            (navigation ? navigation.getBoundingClientRect().bottom : 120) + 10;
          const top =
            scrollingEl.scrollTop +
            el.getBoundingClientRect().top -
            navbarOffset;
          if (scrollingEl.scrollTo) {
            scrollingEl.scrollTo({ top, behavior: 'smooth' });
          } else {
            scrollingEl.scrollTop = top;
          }
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
            [(b) => b.descriptor.displayName]
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
};
</script>

<style lang="css">
.m-bean {
  transition: all ease-out 86ms;
}

.m-bean.is-active {
  margin: 0.75rem -0.75rem;
  max-width: unset;
}

.m-bean.is-active .m-bean--header {
  padding-bottom: 0;
}

.m-bean:not(.is-active) .m-bean--header:hover {
  background-color: #fafafa;
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
  right: 0.75rem;
  top: 0.75rem;
}
</style>
