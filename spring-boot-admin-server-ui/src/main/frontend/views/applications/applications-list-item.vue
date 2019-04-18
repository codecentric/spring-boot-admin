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
  <div class="application-list-item card" :class="{'is-active': isExpanded}">
    <header class="hero application-list-item__header" :class="headerClass" v-on="$listeners">
      <application-summary v-if="!isExpanded" :application="application" />
      <h1 v-else class="title is-size-5" v-text="application.name" />
      <div class="application-list-item__header__actions" @click.stop="">
        <router-link
          class="button icon-button"
          :to="{ name: 'journal', query: { 'application' : application.name } }"
        >
          <font-awesome-icon icon="history" />
        </router-link>
        <sba-icon-button
          :id="`nf-settings-${application.name}`"
          v-if="hasNotificationFiltersSupport"
          @click="$emit('toggle-notification-filter-settings', application)"
          :icon="hasActiveNotificationFilter(application) ? 'bell-slash' : 'bell'"
        />
        <sba-icon-button
          icon="trash"
          v-if="application.isUnregisterable"
          @click="$emit('unregister', application)"
        />
      </div>
    </header>
    <div class="card-content" v-if="isExpanded">
      <instances-list :instances="application.instances">
        <template slot="actions" slot-scope="{instance}">
          <sba-icon-button :id="`nf-settings-${instance.id}`"
                           v-if="hasNotificationFiltersSupport"
                           @click.stop="$emit('toggle-notification-filter-settings', instance)"
                           :icon="hasActiveNotificationFilter(instance) ? 'bell-slash' : 'bell'"
          />
          <sba-icon-button icon="trash"
                           v-if="instance.isUnregisterable"
                           @click.stop="$emit('unregister', instance)"
          />
        </template>
      </instances-list>
    </div>
  </div>
</template>
<script>
  import Application from '@/services/application';
  import ApplicationSummary from './application-summary'
  import InstancesList from './instances-list'

  export default {
    components: {ApplicationSummary, InstancesList},
    props: {
      application: {
        type: Application,
        required: true
      },
      isExpanded: {
        type: Boolean,
        default: false
      },
      notificationFilters: {
        type: Array,
        default: () => []
      },
      hasNotificationFiltersSupport: {
        type: Boolean,
        default: false
      }
    },
    computed: {
      headerClass() {
        if (!this.isExpanded) {
          return 'is-selectable';
        }
        if (this.application.status === 'UP') {
          return 'is-primary';
        }
        if (this.application.status === 'RESTRICTED') {
          return 'is-warning';
        }
        if (this.application.status === 'DOWN') {
          return 'is-danger';
        }
        if (this.application.status === 'OUT_OF_SERVICE') {
          return 'is-danger';
        }
        if (this.application.status === 'OFFLINE') {
          return 'is-light';
        }
        return 'is-light';
      }
    },
    methods: {
      hasActiveNotificationFilter(object) {
        return this.notificationFilters.some(f => f.affects(object));
      }
    }
  }
</script>
<style lang="scss">
  @import "~@/assets/css/utilities";

  .application-list-item {
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

      .title {
        flex-grow: 1;
        flex-basis: 50%;
        margin: 0.75rem 0;
      }

      &__actions {
        justify-self: end;
        opacity: 0;
        transition: all $easing $speed;
        will-change: opacity;
        margin-right: ($gap / 2);
        display: flex;

        *:hover > &,
        *.is-active & {
          opacity: 1;
        }

        & > * {
          width: ($gap / 2);
          height: ($gap / 2);
        }
      }
    }
  }
</style>
