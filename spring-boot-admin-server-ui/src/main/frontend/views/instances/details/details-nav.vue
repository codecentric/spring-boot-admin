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
            v-if="instance.showUrl()"
            :title="instance.registration.serviceUrl"
            class="border-gray-400 ml-1"
            @click="openLink(instance.registration.serviceUrl)"
          >
            <svg
              class="h-4 w-4"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </sba-button>

          <sba-button
            v-if="instance.showUrl()"
            :title="instance.registration.managementUrl"
            class="border-gray-400 ml-1"
            @click="openLink(instance.registration.managementUrl)"
          >
            <svg
              class="h-4 w-4"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </sba-button>

          <sba-button
            v-if="instance.showUrl()"
            :title="instance.registration.healthUrl"
            class="border-gray-400 ml-1"
            @click="openLink(instance.registration.healthUrl)"
          >
            <svg
              class="h-4 w-4"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </sba-button>
        </sba-button-group>
      </div>
    </div>
  </sba-sticky-subnav>
</template>

<script>
import SbaButtonGroup from '@/components/sba-button-group';
import SbaButton from '@/components/sba-button.vue';
import SbaStickySubnav from '@/components/sba-sticky-subnav.vue';

import Application from '@/services/application';
import Instance from '@/services/instance';
import InstanceSwitcher from '@/views/instances/details/instance-switcher';

export default {
  name: 'DetailsNav',
  components: { SbaButton, SbaStickySubnav, SbaButtonGroup, InstanceSwitcher },
  props: {
    application: {
      type: Application,
      default: () => ({}),
    },
    instance: {
      type: Instance,
      required: true,
    },
  },
  methods: {
    openLink(url) {
      window.location.href = url;
    },
  },
};
</script>

<style scoped></style>
