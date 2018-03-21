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
  <div class="applications-list">
    <div class="application-list__item card" :class="{'is-active': selected === application.name}"
         v-for="application in applications" :key="application.name" :id=" application.name"
         v-on-clickaway="(event) => selected === application.name && deselect(event)">
      <header class="hero application-list__item__header"
              :class="getHeaderClass(application)"
              @click.stop="select(application.name)">
        <template v-if="selected !== application.name">
          <sba-status :status="application.status"
                      :date="application.statusTimestamp"
                      class="application-list__item__header__status"/>
          <p class="application-list__item__header__name">
            <span v-text="application.name"/><br>
            <span class="is-muted">
              <a v-if="application.instances.length === 1"
                 v-text="application.instances[0].registration.serviceUrl || application.instances[0].registration.healthUrl"
                 :href="application.instances[0].registration.serviceUrl || application.instances[0].registration.healthUrl"/>
              <span v-else
                    v-text="`${application.instances.length} instances`"/>
            </span>
          </p>
          <p class="application-list__item__header__version" v-text="application.version"/>
          <div class="application-list__item__header__actions">
            <sba-icon-button icon="trash"
                             v-if="application.isUnregisterable"
                             @click.native.stop="unregister(application)"/>
          </div>
        </template>
        <template v-else>
          <h1 class="title is-size-5 application-list__item__header__name" v-text="application.name"/>
          <div class="application-list__item__header__actions">
            <sba-icon-button icon="trash"
                             v-if="application.isUnregisterable"
                             @click.native.stop="unregister(application)"/>
          </div>
        </template>
      </header>
      <div class="card-content" v-if="selected === application.name">
        <table class="table is-hoverable is-selectable is-fullwidth application__instances">
          <tbody>
            <tr v-for="instance in application.instances" :key="instance.id" @click.stop="showDetails(instance)">
              <td class="instance__status">
                <sba-status :status="instance.statusInfo.status" :date="instance.statusTimestamp"/>
              </td>
              <td>
                <a v-text="instance.registration.serviceUrl || instance.registration.healthUrl"
                   :href="instance.registration.serviceUrl || instance.registration.healthUrl"
                   @click.stop/><br>
                <span class="is-muted" v-text="instance.id"/>
              </td>
              <td>
                <span v-text="instance.info.version"/>
              </td>
              <td class="instance__actions">
                <sba-icon-button icon="trash"
                                 v-if="instance.isUnregisterable"
                                 @click.native.stop="unregister(instance)"/>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
  import {directive as onClickaway} from 'vue-clickaway';

  export default {
    props: {
      applications: {
        type: Array,
        default: () => []
      },
      selected: {
        type: String,
        default: null
      }
    },
    directives: {onClickaway},
    data: () => ({
      errors: []
    }),
    methods: {
      select(name) {
        this.$router.replace({name: 'applications', params: {selected: name}});
      },
      deselect(event) {
        if (event && event.target instanceof HTMLAnchorElement) {
          return;
        }
        this.$router.replace({name: 'applications'});
      },
      showDetails(instance) {
        this.$router.push({name: 'instance/details', params: {instanceId: instance.id}});
      },
      async scrollIntoView(id, behavior) {
        if (id) {
          await this.$nextTick();
          const el = document.getElementById(id);
          if (el) {
            const top = el.getBoundingClientRect().top + window.scrollY - 100;
            window.scroll({top, left: window.scrollX, behavior: behavior || 'smooth'});
          }
        }
      },
      getHeaderClass(application) {
        if (this.selected !== application.name) {
          return 'is-selectable';
        }
        if (application.status === 'UP') {
          return 'is-primary';
        }
        if (application.status === 'RESTRICTED') {
          return 'is-warning';
        }
        if (application.status === 'DOWN') {
          return 'is-danger';
        }
        if (application.status === 'OUT_OF_SERVICE') {
          return 'is-danger';
        }
        if (application.status === 'OFFLINE') {
          return 'is-light';
        }
        return 'is-light';
      },
      async unregister(item) {
        try {
          item.unregister();
        } catch (e) {
          this.errors.push(e);
        }
      }
    },
    mounted() {
      return this.scrollIntoView(this.selected, 'instant');
    },
    watch: {
      selected(newVal) {
        return this.scrollIntoView(newVal);
      }
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .application-list__item {
    transition: all $easing $speed;

    &.is-active {
      margin: 0.75rem -0.75rem;
      max-width: unset;
    }

    &__header {
      display: flex;
      flex-direction: row;
      justify-content: flex-start;
      align-items: center;

      *:not(.is-active) > &:hover {
        background-color: $white-bis;
      }

      & > *:not(:first-child) {
        margin-left: 12px;
      }

      &__status {
        width: $gap;
      }

      &__name,
      &__version {
        flex-grow: 1;
        flex-basis: 50%;
      }

      &__name.title {
        margin: 0.75rem 0;
      }

      &__actions {
        justify-self: end;
        opacity: 0;
        transition: all $easing $speed;
        will-change: opacity;
        margin-right: ($gap / 2);

        *:hover > &,
        *.is-active & {
          opacity: 1;
        }
      }
    }
  }

  .application__instances td {
    vertical-align: middle;
  }

  .instance {
    &__status {
      width: $gap;
    }

    &__actions {
      text-align: right;
      opacity: 0;
      transition: all $easing $speed;
      will-change: opacity;
      margin-right: $gap;

      *:hover > & {
        opacity: 1;
      }
    }
  }

</style>
