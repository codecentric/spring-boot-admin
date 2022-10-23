<template>
  <div v-if="otherInstances.length > 0" class="relative inline-block z-50">
    <button
      class="inline-flex justify-center items-center bg-gray-100 w-full rounded-md text-black hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium"
      @click="showInstances = !showInstances"
    >
      <sba-status
        :status="currentInstance.statusInfo.status"
        class="mr-1 hidden md:block"
      />
      <span
        class="hidden sm:block truncate pr-1 w-full"
        v-text="currentInstance.registration.name"
      />
      <span class="block sm:hidden">{{ currentInstance.id }}</span>
      <span class="hidden sm:block">({{ currentInstance.id }})</span>

      <svg
        class="-mr-1 ml-2 h-5 w-5"
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 20 20"
        fill="currentColor"
        aria-hidden="true"
      >
        <path
          d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
        />
      </svg>
    </button>

    <transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0 translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-1"
    >
      <div
        v-if="showInstances"
        v-on-clickaway="() => (showInstances = false)"
        class="absolute -ml-2 mt-3 transform px-2 w-screen max-w-md"
      >
        <div
          class="rounded-lg shadow-lg ring-1 ring-black ring-opacity-5 overflow-hidden overflow-y-auto max-h-32 md:max-h-96"
        >
          <div class="relative grid gap-4 bg-white p-4">
            <a
              v-for="otherInstance in otherInstances"
              :key="otherInstance.id"
              class="-m-3 p-3 flex items-center rounded-lg hover:bg-gray-50"
              @click.stop="switchToInstance(otherInstance)"
            >
              <sba-status
                :status="otherInstance.statusInfo.status"
                class="mr-3"
              />
              <div>
                <div v-text="otherInstance.registration.name" />
                <div class="text-xs italic" v-text="otherInstance.id" />
              </div>
            </a>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { directive as onClickaway } from 'vue3-click-away';

import Instance from '@/services/instance';

export default {
  directives: { onClickaway },
  props: {
    instances: {
      type: Array,
      required: true,
    },
    currentInstance: {
      type: Instance,
      required: true,
    },
  },
  data() {
    return {
      showInstances: false,
    };
  },
  computed: {
    otherInstances() {
      return this.instances
        .filter((i) => i.id !== this.currentInstance.id)
        .sort((a, b) => a.id.localeCompare(b.id));
    },
  },
  methods: {
    switchToInstance(instance) {
      this.showInstances = false;
      this.$router.push({
        name: 'instances/details',
        params: { instanceId: instance.id },
      });
    },
  },
};
</script>

<style scoped></style>
