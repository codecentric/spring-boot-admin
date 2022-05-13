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
  <sba-modal-confirm
    v-model:open="isModalShutdownApplicationOpen"
    :title="$t('applications.actions.shutdown')"
    @close="shutdownApplication($event)"
  >
    <span v-html="$t('applications.shutdown', {name: application.name})" />
  </sba-modal-confirm>

  <sba-modal-confirm
    v-model:open="isModalRestartApplicationOpen"
    :title="$t('applications.actions.restart')"
    @close="restartApplication($event)"
  >
    <span v-html="$t('applications.restart', {name: application.name})" />
  </sba-modal-confirm>

  <sba-modal-confirm
    v-model:open="isModalShutdownInstanceOpen"
    :title="$t('applications.actions.shutdown')"
    @close="shutdownInstance($event)"
  >
    <span v-html="$t('instances.shutdown', {name: currentModalInstance.id})" />
  </sba-modal-confirm>

  <sba-modal-confirm
    v-model:open="isModalShutdownInstanceOpen"
    :title="$t('applications.actions.shutdown')"
    @close="restartInstance($event)"
  >
    <span v-html="$t('instances.restart', {name: currentModalInstance.id})" />
  </sba-modal-confirm>

  <sba-modal-confirm
    v-model:open="isModalUnregisterApplicationOpen"
    :title="$t('applications.actions.unregister')"
    @close="unregisterApplication($event)"
  >
    <span v-html="$t('applications.unregister', {name: application.name})" />
  </sba-modal-confirm>

  <sba-modal-confirm
    v-model:open="isModalUnregisterInstanceOpen"
    :title="$t('applications.actions.unregister')"
    @close="unregisterInstance($event)"
  >
    <span v-html="$t('instances.unregister', {name: currentModalInstance.id})" />
  </sba-modal-confirm>

  <sba-modal-confirm
    v-model:open="isApplicationRestarted"
    :title="$t('applications.actions.restart')"
    skip-cancel
    @close="closeModals"
  >
    <span v-html="$t('applications.restarted', {name: application.name})" />
  </sba-modal-confirm>

  <sba-modal-confirm
    v-model:open="isInstanceRestarted"
    :title="$t('applications.actions.restart')"
    skip-cancel
    @close="closeModals"
  >
    <span v-html="$t('instances.restarted')" />
  </sba-modal-confirm>

  <div
    :id="application.name"
    v-on-clickaway="(event) => $emit('deselect', event, application.name)"
    class="application-list-item"
    :class="{'is-active': isExpanded}"
    @click="$emit('select', application.name)"
  >
    <header
      class="application-list-item__header"
      :class="headerClass"
      v-on="$attrs"
    >
      <ApplicationsListItemSummary
        v-if="!isExpanded"
        :application="application"
      />
      <h1
        v-else
        class="font-bold text-lg"
        v-text="application.name"
      />
      <ApplicationListItemActions
        :application="application"
        :has-active-notification-filter="hasActiveNotificationFilter(application)"
        :has-notification-filters-support="hasNotificationFiltersSupport"
        @restart="confirmRestartApplication"
        @shutdown="confirmShutdownApplication"
        @filter-settings="$emit('toggle-notification-filter-settings', application)"
        @unregister="isModalUnregisterApplicationOpen = true"
      />
    </header>
    <!-- EXPANDED -->
    <ul
      v-if="isExpanded"
      class="pt-2"
    >
      <instances-list :instances="application.instances">
        <template #actions="{instance}">
          <sba-button-group class="hidden md:flex">
            <sba-button
              v-if="hasNotificationFiltersSupport"
              :id="`nf-settings-${instance.id}`"
              size="xs"
              @click.stop="$emit('toggle-notification-filter-settings', instance)"
            >
              <font-awesome-icon
                :icon="hasActiveNotificationFilter(instance) ? 'bell-slash' : 'bell'"
                class="h-3"
              />
            </sba-button>
            <sba-button
              v-if="instance.isUnregisterable"
              title="unregister"
              size="xs"
              @click.stop="confirmUnregisterInstance(instance)"
            >
              <font-awesome-icon
                icon="trash"
                class="h-3"
              />
            </sba-button>
            <sba-button
              v-if="instance.hasEndpoint('shutdown')"
              title="shutdown"
              size="xs"

              @click.stop="confirmShutdownInstance(instance)"
            >
              <font-awesome-icon
                :icon="['far', 'stop-circle']"
                class="h-3"
              />
            </sba-button>
            <sba-button
              v-if="instance.hasEndpoint('restart')"
              title="restart"
              size="xs"
              @click.stop="confirmRestartInstance(instance)"
            >
              <font-awesome-icon
                icon="sync-alt"
                class="h-3"
              />
            </sba-button>
          </sba-button-group>
        </template>
      </instances-list>
    </ul>
  </div>
</template>

<script>
import Application from '../../services/application';
import InstancesList from './instances-list.vue';
import {HealthStatus} from '../../HealthStatus.js';
import ApplicationListItemActions from "./ApplicationListItemActions.vue";
import ApplicationsListItemSummary from "./applications-list-item-summary.vue";
import {directive as onClickaway} from 'vue3-click-away';

export default {
  components: {ApplicationsListItemSummary, ApplicationListItemActions, InstancesList},
  directives: {onClickaway},
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
    },
  },
  emits: ['unregister', 'toggle-notification-filter-settings', 'shutdown', 'restart', 'deselected', 'deselect', 'select'],
  data() {
    return {
      isModalShutdownApplicationOpen: false,
      isModalRestartApplicationOpen: false,
      isModalUnregisterApplicationOpen: false,
      isModalShutdownInstanceOpen: false,
      isModalRestartInstanceOpen: false,
      isModalUnregisterInstanceOpen: false,
      isApplicationRestarted: false,
      isInstanceRestarted: false,
      currentModalInstance: undefined
    }
  },
  computed: {
    headerClass() {
      if (!this.isExpanded) {
        return 'is-selectable';
      }
      if (this.application.status === HealthStatus.UP) {
        return 'is-primary';
      }
      if (this.application.status === HealthStatus.RESTRICTED) {
        return 'is-warning';
      }
      if (this.application.status === HealthStatus.DOWN) {
        return 'is-danger';
      }
      if (this.application.status === HealthStatus.OUT_OF_SERVICE) {
        return 'is-danger';
      }
      if (this.application.status === HealthStatus.OFFLINE) {
        return 'is-light';
      }
      return 'is-light';
    }
  },
  methods: {
    hasActiveNotificationFilter(object) {
      return this.notificationFilters.some(f => f.affects(object));
    },
    confirmShutdownApplication() {
      this.isModalShutdownApplicationOpen = true;
    },
    confirmShutdownInstance(instance) {
      this.isModalShutdownInstanceOpen = true;
      this.currentModalInstance = instance;
    },
    confirmUnregisterInstance(instance) {
      this.isModalUnregisterInstanceOpen = true;
      this.currentModalInstance = instance;
    },
    confirmRestartApplication() {
      this.isModalRestartApplicationOpen = true;
    },
    confirmRestartInstance(instance) {
      this.isModalRestartInstanceOpen = true;
      this.currentModalInstance = instance;
    },
    closeModals() {
      this.isModalShutdownApplicationOpen = false;
      this.isModalShutdownInstanceOpen = false;
      this.isModalRestartApplicationOpen = false;
      this.isModalRestartInstanceOpen = false;
      this.isApplicationRestarted = false;
      this.isInstanceRestarted = false;
      this.isModalUnregisterApplicationOpen = false;
      this.isModalUnregisterInstanceOpen = false;
    },
    shutdownApplication($event) {
      this.closeModals();
      if ($event === true) {
        this.$emit('shutdown', this.application);
      }
    },
    shutdownInstance($event) {
      this.closeModals();
      if ($event === true) {
        this.$emit('shutdown', this.currentModalInstance);
      }
    },
    unregisterApplication($event) {
      this.closeModals();
      if ($event === true) {
        this.$emit('unregister', this.application)
      }
    },
    restartApplication($event) {
      this.closeModals();
      if ($event === true) {
        this.$emit('restart', this.application);
        this.isApplicationRestarted = true;
      }
    },
    restartInstance($event) {
      this.closeModals();
      if ($event === true) {
        this.$emit('restart', this.currentModalInstance);
        this.isInstanceRestarted = true;
      }
    },
    unregisterInstance($event) {
      this.closeModals();
      if ($event === true) {
        this.$emit('unregister', this.currentModalInstance)
      }
    },
  }
}
</script>

<style>
.application-list-item {
  transition: all ease-out 250ms;
}

.application-list-item:not(.is-active) {
  @apply cursor-pointer;
}

.application-list-item.is-active {
  @apply border-t border-b bg-gray-50;
  max-width: unset;
}

.application-list-item.is-active:first-child {
  @apply border-t-0;
}

.application-list-item.is-active:last-child {
  @apply border-b-0;
}

.application-list-item.is-active li {
  @apply pl-4 pr-4 cursor-pointer;
}

.application-list-item.is-active .application-list-item__header {
  @apply pb-1;
}

.application-list-item__header {
  @apply px-4 py-3 flex justify-between items-center;
}

.application-list-item:not(.is-active):hover {
  @apply bg-gray-100 cursor-pointer;
}

.application-list-item__header .title {
  flex-grow: 1;
  flex-basis: 50%;
  margin: 0.75rem 0;
}
</style>
