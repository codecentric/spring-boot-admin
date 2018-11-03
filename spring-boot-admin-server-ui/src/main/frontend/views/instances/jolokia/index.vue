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
    <div v-if="error" class="message is-danger">
      <div class="message-body">
        <strong>
          <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
          Fetching JMX Beans failed.
        </strong>
        <p v-text="error.message" />
      </div>
    </div>
    <div class="columns">
      <div class="column" v-if="selectedDomain">
        <h1 class="heading">MBeans</h1>
        <div class="m-bean card" :class="{'is-active': mBean === selectedMBean}"
             v-for="mBean in selectedDomain.mBeans" :key="mBean.descriptor.raw" :id="mBean.descriptor.raw"
             v-on-clickaway="() => mBean === selectedMBean && select(selectedDomain)"
        >

          <header class="m-bean--header hero"
                  :class="{'is-primary': mBean === selectedMBean, 'is-selectable' : mBean !== selectedMBean }"
                  @click="select(selectedDomain, mBean)"
          >
            <div class="level is-clipped">
              <div class="level-left">
                <div class="level-item is-narrow"
                     v-for="attribute in mBean.descriptor.attributes"
                     :key="`mBean-desc-${attribute.name}`"
                >
                  <div class="is-clipped" :title="`${attribute.name} ${attribute.value}`">
                    <p class="heading" v-text="attribute.name" />
                    <p class="title is-size-6" v-text="attribute.value" />
                  </div>
                </div>
              </div>
            </div>
            <sba-icon-button v-if="mBean === selectedMBean" :icon="['far', 'times-circle']"
                             class="m-bean--header--close has-text-white"
                             @click.stop="select(selectedDomain)"
            />
            <div class="hero-foot tabs is-boxed" v-if="mBean === selectedMBean">
              <ul>
                <li v-if="mBean.attr" :class="{'is-active' : selected.view === 'attributes' }">
                  <a @click.stop="select(selectedDomain, selectedMBean, 'attributes')">Attributes</a>
                </li>
                <li v-if="mBean.op" :class="{'is-active' : selected.view === 'operations' }">
                  <a @click.stop="select(selectedDomain, selectedMBean, 'operations')">Operations</a>
                </li>
              </ul>
            </div>
          </header>

          <div class="card-content" v-if="mBean === selectedMBean">
            <m-bean-attributes v-if="selected.view === 'attributes'" :instance="instance"
                               :domain="selectedDomain.domain" :m-bean="mBean"
            />
            <m-bean-operations v-if="selected.view === 'operations'" :instance="instance"
                               :domain="selectedDomain.domain" :m-bean="mBean"
            />
          </div>
        </div>
      </div>
      <div class="column is-narrow">
        <nav class="menu" v-sticks-below="['#navigation']">
          <p class="menu-label">domains</p>
          <ul class="menu-list">
            <li>
              <a class="" v-for="domain in domains" :key="domain.domain"
                 :class="{'is-active' : domain === selectedDomain}"
                 v-text="domain.domain" @click="select(domain)"
              />
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </section>
</template>

<script>
  import sticksBelow from '@/directives/sticks-below';
  import Instance from '@/services/instance';
  import fromPairs from 'lodash/fromPairs';
  import isEmpty from 'lodash/isEmpty';
  import isEqual from 'lodash/isEqual';
  import sortBy from 'lodash/sortBy';
  import {directive as onClickaway} from 'vue-clickaway';
  import mBeanAttributes from './m-bean-attributes';
  import mBeanOperations from './m-bean-operations';

  const getOperationName = (name, descriptor) => {
    const params = descriptor.args.map(arg => arg.type).join(',');
    return `${name}(${params})`;
  };

  class MBeanDescriptor {
    constructor(raw) {
      Object.assign(this, MBeanDescriptor.parse(raw));
      this.raw = raw;
    }

    static parse(raw) {
      const attributes = raw.split(',')
        .map(attribute => attribute.split('='))
        .map(([name, value]) => ({name, value}));
      const displayName = attributes.map(({value}) => value).join(' ').trim();
      return {attributes, displayName}
    }
  }

  export class MBean {
    constructor({descriptor, op, ...mBean}) {
      Object.assign(this, mBean);
      this.descriptor = new MBeanDescriptor(descriptor);
      const flattenedOps = Object.entries(op || {}).flatMap(([name, value]) => {
        if (Array.isArray(value)) {
          return value.map(v => [name, v]);
        } else {
          return [[name, value]];
        }
      }).map(([name, operation]) => [getOperationName(name, operation), operation]);
      this.op = flattenedOps.length > 0 ? fromPairs(flattenedOps) : null;
    }
  }

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    // eslint-disable-next-line vue/no-unused-components
    components: {mBeanOperations, mBeanAttributes},
    directives: {onClickaway, sticksBelow},
    data: () => ({
      hasLoaded: false,
      error: null,
      domains: [],
      selected: {
        domain: null,
        mBean: null,
        view: null
      }
    }),
    computed: {
      selectedDomain() {
        return this.domains.find(d => d.domain === this.selected.domain)
      },
      selectedMBean() {
        return this.selectedDomain && this.selectedDomain.mBeans.find(b => b.descriptor.raw === this.selected.mBean)
      }
    },
    created() {
      this.fetchMBeans();
    },
    watch: {
      '$route': {
        immediate: true,
        handler() {
          if (!isEmpty(this.$route.query)) {
            this.selected = this.$route.query;
          } else if (this.domains.length > 0) {
            this.select(this.domains[0]);
          }
        }
      },
      selected() {
        if (!isEqual(this.selected, this.$route.query)) {
          this.$router.replace({
            name: 'instances/jolokia',
            query: this.selected
          });
        }
      },
      async selectedMBean(newVal) {
        if (newVal) {
          await this.$nextTick();
          const el = document.getElementById(newVal.descriptor.raw);
          if (el) {
            const scrollingEl = document.scrollingElement;
            const navigation = document.querySelector('#navigation');
            const navbarOffset = (navigation ? navigation.getBoundingClientRect().bottom : 120) + 10;
            const top = scrollingEl.scrollTop + el.getBoundingClientRect().top - navbarOffset;
            if (scrollingEl.scrollTo) {
              scrollingEl.scrollTo({top, behavior: 'smooth'})
            } else {
              scrollingEl.scrollTop = top;
            }
          }
        }
      }
    },
    methods: {
      async fetchMBeans() {
        this.error = null;
        try {
          const res = await this.instance.listMBeans();
          const domains = sortBy(res.data, [d => d.domain]);
          this.domains = domains.map(domain => ({
            ...domain,
            mBeans: sortBy(domain.mBeans.map(mBean => new MBean(mBean)), [b => b.descriptor.displayName])
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
        this.selected = {
          domain: domain && domain.domain,
          mBean: mBean && mBean.descriptor.raw,
          view: view || (mBean ? (mBean.attr ? 'attributes' : (mBean.op ? 'operations' : null)) : null)
        };
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/jolokia',
        parent: 'instances',
        path: 'jolokia',
        component: this,
        label: 'JMX',
        group: 'JVM',
        order: 350,
        isEnabled: ({instance}) => instance.hasEndpoint('jolokia')
      });
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .m-bean {
    transition: all $easing $speed;

    &.is-active {
      margin: 0.75rem -0.75rem;
      max-width: unset;
    }

    &.is-active .m-bean--header {
      padding-bottom: 0;
    }

    &:not(.is-active) .m-bean--header:hover {
      background-color: $white-bis;
    }

    &--header {
      & .level .level-left {
        width: 100%;

        & .level-item {
          min-width: 0;
          flex-shrink: 1;

          & p {
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
          }
        }
      }

      &--close {
        position: absolute;
        right: 0.75rem;
        top: 0.75rem;
      }
    }
  }
</style>
