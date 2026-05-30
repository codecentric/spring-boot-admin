<template>
  <sba-button-group class="application-list-item__header__actions text-right">
    <router-link v-slot="{ navigate }" :to="journalLink" custom>
      <sba-button
        :size="size === 'xs' ? '2xs' : undefined"
        :title="t('applications.actions.journal')"
        @click.stop="navigate"
      >
        <font-awesome-icon :icon="faScroll" :size="size" />
      </sba-button>
    </router-link>
    <sba-button
      v-if="hasNotificationFiltersSupport"
      :id="`nf-settings-${item.name || item.id}`"
      :size="size === 'xs' ? '2xs' : undefined"
      :title="$t('applications.actions.notification_filters')"
      @click.stop="$emit('filter-settings', item)"
    >
      <font-awesome-icon
        :size="size"
        :icon="hasActiveNotificationFilter ? faBellSlash : faBell"
      />
    </sba-button>
    <sba-button
      v-if="item.isUnregisterable"
      class="btn-unregister"
      :size="size === 'xs' ? '2xs' : undefined"
      :title="t('applications.actions.unregister')"
      @click.stop="actionHandler.unregister(item)"
    >
      <font-awesome-icon :size="size" :icon="faTrash" />
    </sba-button>
    <sba-button
      v-if="item.hasEndpoint('restart')"
      :size="size === 'xs' ? '2xs' : undefined"
      :title="t('applications.actions.restart')"
      @click.stop="actionHandler.restart(item)"
    >
      <font-awesome-icon :size="size" :icon="faUndoAlt" />
    </sba-button>
    <sba-button
      v-if="item.hasEndpoint('shutdown')"
      :size="size === 'xs' ? '2xs' : undefined"
      :title="t('applications.actions.shutdown')"
      class="is-danger btn-shutdown"
      @click.stop="actionHandler.shutdown(item)"
    >
      <font-awesome-icon :size="size" :icon="faPowerOff" />
    </sba-button>
  </sba-button-group>
</template>

<script lang="ts" setup>
import {
  faBell,
  faBellSlash,
  faPowerOff,
  faScroll,
  faTrash,
  faUndoAlt,
} from '@fortawesome/free-solid-svg-icons';
import { useNotificationCenter } from '@stekoe/vue-toast-notificationcenter';
import { PropType, inject } from 'vue';
import { useI18n } from 'vue-i18n';
import { RouteLocationNamedRaw } from 'vue-router';

import SbaButtonGroup from '@/components/sba-button-group.vue';

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
  size: {
    type: String as PropType<'xs' | 'sm'>,
    default: 'sm',
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
@reference "../../index.css";
.application-list-item__header__actions {
  @apply hidden lg:inline-flex p-1 bg-black/5 rounded-lg;
}
</style>
