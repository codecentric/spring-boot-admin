<template>
  <TransitionRoot
    ref="root"
    :show="isOpen"
    appear
    as="template"
    @close="closeModal"
  >
    <Dialog as="div" class="relative z-10 modal">
      <TransitionChild
        as="template"
        enter="duration-300 ease-out"
        enter-from="opacity-0"
        enter-to="opacity-100"
        leave="duration-200 ease-in"
        leave-from="opacity-100"
        leave-to="opacity-0"
      >
        <div class="fixed inset-0 bg-black bg-opacity-25" />
      </TransitionChild>

      <div class="fixed inset-0 overflow-y-auto">
        <div
          class="flex min-h-full items-center justify-center p-4 text-center"
        >
          <TransitionChild
            as="template"
            enter="duration-300 ease-out"
            enter-from="opacity-0 scale-95"
            enter-to="opacity-100 scale-100"
            leave="duration-200 ease-in"
            leave-from="opacity-100 scale-100"
            leave-to="opacity-0 scale-95"
          >
            <DialogPanel
              class="w-full max-w-md transform overflow-hidden rounded bg-white p-6 text-left align-middle shadow-xl transition-all"
            >
              <DialogTitle
                as="h3"
                class="text-lg font-medium leading-6 text-gray-900 flex justify-between"
              >
                {{ title }}
              </DialogTitle>
              <div class="mt-2 mb-2">
                <slot name="body" />
              </div>
              <div class="mt-4">
                <slot name="buttons" />
              </div>
            </DialogPanel>
          </TransitionChild>
        </div>
      </div>
    </Dialog>
  </TransitionRoot>
</template>

<script>
import {
  Dialog,
  DialogPanel,
  DialogTitle,
  TransitionChild,
  TransitionRoot,
} from '@headlessui/vue';
import { defineComponent, ref } from 'vue';

import eventBus from '@/services/bus';

export default defineComponent({
  components: {
    Dialog,
    DialogPanel,
    DialogTitle,
    TransitionRoot,
    TransitionChild,
  },
  props: {
    title: {
      type: String,
      required: true,
    },
  },
  emits: ['close'],
  setup() {
    return {
      isOpen: ref(true),
    };
  },
  mounted() {
    eventBus.on('sba-modal-close', this.closeModal);
  },
  beforeUnmount() {
    eventBus.off('sba-modal-close', this.closeModal);
  },
  methods: {
    closeModal() {
      this.isOpen = false;
    },
  },
});
</script>
