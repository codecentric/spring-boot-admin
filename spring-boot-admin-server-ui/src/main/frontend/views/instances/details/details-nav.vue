<template>
  <sba-sticky-subnav>
    <div class="flex">
      <div class="flex-2">
        <instance-switcher
          v-if="instance && application"
          :current-instance="instance"
          :instances="application.instances"
        />
      </div>
      <div class="flex-1 text-right">
        <sba-button-group>
          <sba-button
            v-if="showUrl && !isUrlDisabled"
            as="a"
            referrerpolicy="no-referrer"
            target="_blank"
            :title="instance?.registration?.serviceUrl"
            :href="instance?.registration?.serviceUrl"
            class="border-gray-400"
          >
            <font-awesome-icon :icon="faHome" />
          </sba-button>

          <sba-button
            v-if="showUrl"
            as="a"
            referrerpolicy="no-referrer"
            target="_blank"
            :title="instance?.registration?.managementUrl"
            :href="instance?.registration?.managementUrl"
            class="border-gray-400"
          >
            <font-awesome-icon :icon="faCogs" />
          </sba-button>

          <sba-button
            v-if="showUrl"
            as="a"
            referrerpolicy="no-referrer"
            target="_blank"
            :title="instance?.registration?.healthUrl"
            :href="instance?.registration?.healthUrl"
            class="border-gray-400"
          >
            <font-awesome-icon :icon="faHeartPulse" />
          </sba-button>
        </sba-button-group>
      </div>
    </div>
  </sba-sticky-subnav>
</template>

<script setup lang="ts">
import {
  faCogs,
  faHeartPulse,
  faHome,
} from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { computed } from 'vue';

import SbaButtonGroup from '@/components/sba-button-group';
import SbaButton from '@/components/sba-button.vue';
import SbaStickySubnav from '@/components/sba-sticky-subnav.vue';

import { useInstanceData } from '@/composables/useInstanceData';
import { useSbaConfig } from '@/sba-config';
import InstanceSwitcher from '@/views/instances/details/instance-switcher';

const props = defineProps<{
  instanceId: string;
}>();

const { instance, application } = useInstanceData(props.instanceId);

const showUrl = computed(() => {
  const sbaConfig = useSbaConfig();
  if (sbaConfig.uiSettings.hideInstanceUrl) return false;
  const hideUrlMetadata = instance.value?.registration?.metadata?.['hide-url'];
  return hideUrlMetadata !== 'true';
});

const isUrlDisabled = computed(() => {
  const sbaConfig = useSbaConfig();
  if (sbaConfig.uiSettings.disableInstanceUrl) return true;
  const disableUrl = instance.value?.registration?.metadata?.['disable-url'];
  return disableUrl === 'true';
});
</script>
