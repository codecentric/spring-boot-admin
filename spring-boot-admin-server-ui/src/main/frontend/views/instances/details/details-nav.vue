<template>
  <sba-sticky-subnav>
    <div class="flex">
      <div class="flex-2">
        <instance-switcher
          :current-instance="instance"
          :instances="application.instances"
        />
      </div>
      <div class="flex-1 text-right">
        <sba-button-group>
          <sba-button
            v-if="instance.showUrl() && !instance.isUrlDisabled()"
            as="a"
            referrerpolicy="no-referrer"
            target="_blank"
            :title="instance.registration.serviceUrl"
            :href="instance.registration.serviceUrl"
            class="border-gray-400 ml-1"
          >
            <font-awesome-icon :icon="faHome" />
          </sba-button>

          <sba-button
            v-if="instance.showUrl()"
            as="a"
            referrerpolicy="no-referrer"
            target="_blank"
            :title="instance.registration.managementUrl"
            :href="instance.registration.managementUrl"
            class="border-gray-400 ml-1"
          >
            <font-awesome-icon :icon="faClipboardList" />
          </sba-button>

          <sba-button
            v-if="instance.showUrl()"
            as="a"
            referrerpolicy="no-referrer"
            target="_blank"
            :title="instance.registration.healthUrl"
            :href="instance.registration.healthUrl"
            class="border-gray-400 ml-1"
          >
            <font-awesome-icon :icon="faHeart" />
          </sba-button>
        </sba-button-group>
      </div>
    </div>
  </sba-sticky-subnav>
</template>

<script setup lang="ts">
import {
  faClipboardList,
  faHeart,
  faHome,
} from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

import SbaButtonGroup from '@/components/sba-button-group';
import SbaButton from '@/components/sba-button.vue';
import SbaStickySubnav from '@/components/sba-sticky-subnav.vue';

import Application from '@/services/application';
import Instance from '@/services/instance';
import InstanceSwitcher from '@/views/instances/details/instance-switcher';

defineProps<{
  application: Application;
  instance: Instance;
}>();
</script>
