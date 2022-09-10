<template>
  <sba-button-group
    class="application-list-item__header__actions text-right"
  >
    <router-link
      v-if="isApplication"
      v-slot="{ navigate }"
      :to="{ name: 'journal', query: { 'application' : item.name } }"
      custom
    >
      <sba-button
        :title="$t('applications.actions.journal')"
        @click.stop="navigate"
      >
        <font-awesome-icon icon="history"/>
      </sba-button>
    </router-link>
    <sba-button
      v-if="hasNotificationFiltersSupport"
      :id="`nf-settings-${item.name || item.id}`"
      :title="$t('applications.actions.notification_filters')"
      @click.stop="$emit('filter-settings', item)"
    >
      <font-awesome-icon :icon="hasActiveNotificationFilter ? 'bell-slash' : 'bell'"/>
    </sba-button>
    &nbsp;
    <sba-button
      v-if="item.isUnregisterable"
      :title="$t('applications.actions.unregister')"
      @click.stop="$emit('unregister', item)"
    >
      <font-awesome-icon :icon="'trash'"/>
    </sba-button>
    <sba-button
      v-if="item.hasEndpoint('restart')"
      :title="$t('applications.actions.restart')"
      @click.stop="$emit('restart', item)"
    >
      <font-awesome-icon icon="sync-alt"/>
    </sba-button>
    &nbsp;
    <sba-button
      v-if="item.hasEndpoint('shutdown')"
      :title="$t('applications.actions.shutdown')"
      class="is-danger"
      @click.stop="$emit('shutdown', item)"
    >
      <font-awesome-icon :icon="['fa', 'power-off']"/>
    </sba-button>
  </sba-button-group>
</template>
<script>
import Application from "../../services/application.js";
import Instance from "../../services/instance";

export default {
  name: 'ApplicationListItemActions',
  props: {
    item: {
      type: [Application, Instance],
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
  setup(props) {
    return {
      isApplication: props.item instanceof Application
    }
  },
  emits: ['filter-settings', 'unregister', 'shutdown', 'restart']
}
</script>

<style scoped>
.application-list-item__header__actions {
  @apply hidden md:flex;
  justify-content: flex-end;
  opacity: 0;
  transition: all ease-out 86ms;
  will-change: opacity;
}

*:hover > .application-list-item__header__actions, *.is-active .application-list-item__header__actions {
  opacity: 1;
}
</style>
