<template>
  <sba-button-group class="application-list-item__header__actions text-right">
    <router-link v-slot="{ navigate }" :to="journalLink" custom>
      <sba-button
        :title="$t('applications.actions.journal')"
        @click.stop="navigate"
      >
        <font-awesome-icon icon="history" />
      </sba-button>
    </router-link>
    <sba-button
      v-if="hasNotificationFiltersSupport"
      :id="`nf-settings-${item.name || item.id}`"
      :title="$t('applications.actions.notification_filters')"
      @click.stop="$emit('filter-settings', item)"
    >
      <font-awesome-icon
        :icon="hasActiveNotificationFilter ? 'bell-slash' : 'bell'"
      />
    </sba-button>
    <sba-button
      v-if="item.isUnregisterable"
      class="btn-unregister"
      :title="$t('applications.actions.unregister')"
      @click.stop="$emit('unregister', item)"
    >
      <font-awesome-icon :icon="'trash'" />
    </sba-button>
    <sba-button
      v-if="item.hasEndpoint('restart')"
      :title="$t('applications.actions.restart')"
      @click.stop="$emit('restart', item)"
    >
      <font-awesome-icon icon="undo-alt" />
    </sba-button>
    <sba-button
      v-if="item.hasEndpoint('shutdown')"
      :title="$t('applications.actions.shutdown')"
      class="is-danger btn-shutdown"
      @click.stop="$emit('shutdown', item)"
    >
      <font-awesome-icon :icon="['fa', 'power-off']" />
    </sba-button>
  </sba-button-group>
</template>
<script>
import Application from '@/services/application';
import Instance from '@/services/instance';

export default {
  name: 'ApplicationListItemActions',
  props: {
    item: {
      type: [Application, Instance],
      required: true,
    },
    hasActiveNotificationFilter: {
      type: Boolean,
      default: false,
    },
    hasNotificationFiltersSupport: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['filter-settings', 'unregister', 'shutdown', 'restart'],
  setup(props) {
    let isApplication = props.item instanceof Application;

    let journalLink;
    if (isApplication) {
      journalLink = {
        name: 'journal',
        query: { application: props.item.name },
      };
    } else {
      journalLink = { name: 'journal', query: { instanceId: props.item.id } };
    }

    return {
      journalLink,
      isApplication,
    };
  },
};
</script>

<style scoped>
.application-list-item__header__actions {
  @apply hidden lg:inline-flex p-1 bg-black/5 rounded-lg;
}

.btn-shutdown,
.btn-unregister {
  @apply ml-1 !important;
}
</style>
