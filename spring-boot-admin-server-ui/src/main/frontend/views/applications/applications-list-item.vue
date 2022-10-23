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
  <div
    :id="application.name"
    v-on-clickaway="(event) => $emit('deselect', event, application.name)"
    class="application-list-item"
    :class="{ 'is-active': isExpanded }"
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
      <h2
        v-if="isExpanded"
        class="font-bold text-lg"
        v-text="application.name"
      />
      <div>
        <ApplicationListItemAction
          :item="application"
          :has-active-notification-filter="
            hasActiveNotificationFilter(application)
          "
          :has-notification-filters-support="hasNotificationFiltersSupport"
          @filter-settings="toggleFilterSettings"
          @restart="confirmRestartApplication"
          @shutdown="confirmShutdownApplication"
          @unregister="confirmUnregisterApplication"
        />
      </div>
    </header>

    <ul v-if="isExpanded" class="pt-2">
      <instances-list :instances="application.instances">
        <template #actions="{ instance }">
          <ApplicationListItemAction
            class="hidden md:flex"
            :item="instance"
            :has-active-notification-filter="
              hasActiveNotificationFilter(instance)
            "
            :has-notification-filters-support="hasNotificationFiltersSupport"
            @filter-settings="toggleFilterSettings"
            @restart="confirmRestartInstance"
            @shutdown="confirmShutdownInstance"
            @unregister="confirmUnregisterInstance"
          />
        </template>
      </instances-list>
    </ul>
  </div>
</template>

<script>
import { directive as onClickaway } from 'vue3-click-away';

import Application from '@/services/application';
import ApplicationListItemAction from '@/views/applications/application-list-item-action';
import ApplicationsListItemSummary from '@/views/applications/applications-list-item-summary';
import InstancesList from '@/views/applications/instances-list';

export default {
  components: {
    ApplicationListItemAction,
    ApplicationsListItemSummary,
    InstancesList,
  },
  directives: { onClickaway },
  props: {
    application: {
      type: Application,
      required: true,
    },
    isExpanded: {
      type: Boolean,
      default: false,
    },
    notificationFilters: {
      type: Array,
      default: () => [],
    },
    hasNotificationFiltersSupport: {
      type: Boolean,
      default: false,
    },
  },
  emits: [
    'unregister',
    'toggle-notification-filter-settings',
    'shutdown',
    'restart',
    'deselected',
    'deselect',
    'select',
  ],
  computed: {
    headerClass() {
      if (!this.isExpanded) {
        return 'is-selectable';
      }
      return '';
    },
  },
  methods: {
    hasActiveNotificationFilter(item) {
      return this.notificationFilters.some((filter) => filter.affects(item));
    },
    toggleFilterSettings(item) {
      this.$emit('toggle-notification-filter-settings', item);
    },
    async confirmShutdownApplication(application) {
      const isConfirmed = await this.$sbaModal.confirm(
        this.$t('applications.actions.shutdown'),
        this.$t('applications.shutdown', { name: application.name })
      );
      if (isConfirmed) {
        this.$emit('restart', application);
      }
    },
    async confirmUnregisterApplication(application) {
      const isConfirmed = await this.$sbaModal.confirm(
        this.$t('applications.actions.unregister'),
        this.$t('applications.unregister', { name: application.name })
      );
      if (isConfirmed) {
        this.$emit('unregister', application);
      }
    },
    async confirmUnregisterInstance(instance) {
      const isConfirmed = await this.$sbaModal.confirm(
        this.$t('applications.actions.unregister'),
        this.$t('applications.unregister', { name: instance.id })
      );
      if (isConfirmed) {
        this.$emit('unregister', instance);
      }
    },
    async confirmRestartApplication(application) {
      const isConfirmed = await this.$sbaModal.confirm(
        this.$t('applications.actions.restart'),
        this.$t('applications.restart', { name: application.name })
      );
      if (isConfirmed) {
        this.$emit('restart', application);
      }
    },
    async confirmRestartInstance(instance) {
      const isConfirmed = await this.$sbaModal.confirm(
        this.$t('applications.actions.restart'),
        this.$t('instances.restart', { name: instance.id })
      );
      if (isConfirmed) {
        this.$emit('restart', instance);
      }
    },
    async confirmShutdownInstance(instance) {
      const isConfirmed = await this.$sbaModal.confirm(
        this.$t('applications.actions.shutdown'),
        this.$t('instances.shutdown', { name: instance.id })
      );
      if (isConfirmed) {
        this.$emit('restart', instance);
      }
    },
  },
};
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
