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
      @click.stop="actionHandler.unregister(item)"
    >
      <font-awesome-icon :icon="'trash'" />
    </sba-button>
    <sba-button
      v-if="item.hasEndpoint('restart')"
      :title="$t('applications.actions.restart')"
      @click.stop="actionHandler.restart(item)"
    >
      <font-awesome-icon icon="undo-alt" />
    </sba-button>
    <sba-button
      v-if="item.hasEndpoint('shutdown')"
      :title="$t('applications.actions.shutdown')"
      class="is-danger btn-shutdown"
      @click.stop="actionHandler.shutdown(item)"
    >
      <font-awesome-icon :icon="['fa', 'power-off']" />
    </sba-button>
  </sba-button-group>
</template>

<script lang="ts" setup>
import { useNotificationCenter } from '@stekoe/vue-toast-notificationcenter';
import { inject } from 'vue';
import { useI18n } from 'vue-i18n';
import { RouteLocationNamedRaw } from 'vue-router';

import Application from '@/services/application';
import Instance from '@/services/instance';
import {
  ActionHandler,
  ApplicationActionHandler,
  InstanceActionHandler,
} from '@/views/applications/ActionHandler';

const $sbaModal = inject('$sbaModal');
const { t } = useI18n();
const notificationCenter = useNotificationCenter({});

const props = defineProps({
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
});

defineEmits(['filter-settings']);

let journalLink: RouteLocationNamedRaw;
let actionHandler: ActionHandler;
if (props.item instanceof Application) {
  actionHandler = new ApplicationActionHandler(
    $sbaModal,
    t,
    notificationCenter,
  );
  journalLink = {
    name: 'journal',
    query: { application: props.item.name },
  };
} else if (props.item instanceof Instance) {
  actionHandler = new InstanceActionHandler($sbaModal, t, notificationCenter);
  journalLink = { name: 'journal', query: { instanceId: props.item.id } };
}
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
