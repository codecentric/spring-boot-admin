<template>
  <sba-button-group
    class="application-list-item__header__actions"
  >
    <router-link
      v-slot="{ navigate }"
      :to="{ name: 'journal', query: { 'application' : application.name } }"
      custom
    >
      <sba-button
        :title="$t('applications.actions.journal')"
        @click.stop="navigate"
      >
        <font-awesome-icon icon="history" />
      </sba-button>
    </router-link>
    <sba-button
      v-if="hasNotificationFiltersSupport"
      :id="`nf-settings-${application.name}`"
      :title="$t('applications.actions.notification_filters')"
      @click.stop="$emit('filter-settings', application)"
    >
      <font-awesome-icon :icon="hasActiveNotificationFilter ? 'bell-slash' : 'bell'" />
    </sba-button>
    <sba-button
      v-if="application.isUnregisterable"
      :title="$t('applications.actions.unregister')"
      @click.stop="$emit('unregister', application)"
    >
      <font-awesome-icon :icon="'trash'" />
    </sba-button>
    <sba-button
      v-if="application.hasShutdownEndpoint"
      :title="$t('applications.actions.shutdown')"
      @click.stop="$emit('shutdown', application)"
    >
      <font-awesome-icon :icon="['far', 'stop-circle']" />
    </sba-button>
    <sba-button
      v-if="application.hasRestartEndpoint"
      :title="$t('applications.actions.restart')"
      @click="$emit('restart', application)"
    >
      <font-awesome-icon icon="sync-alt" />
    </sba-button>
  </sba-button-group>
</template>
<script>
import Application from "../../services/application.js";

export default {
  name: 'ApplicationListItemActions',
  props: {
    application: {
      type: Application,
      required: true
    },
    hasActiveNotificationFilter: {
      type: Boolean,
      default: false
    },
    hasNotificationFiltersSupport: {
      type: Boolean,
      default: false
    },
  },
  emits: ['filter-settings', 'unregister', 'shutdown', 'restart']
}
</script>

<style scoped>
.application-list-item__header__actions {
  @apply hidden md:flex;
  justify-self: end;
  opacity: 0;
  transition: all ease-out 86ms;
  will-change: opacity;
}

*:hover > .application-list-item__header__actions, *.is-active .application-list-item__header__actions {
  opacity: 1;
}
</style>
