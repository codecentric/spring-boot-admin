<template>
  <TransitionRoot :show="modelValue" appear as="template">
    <Dialog as="div" class="relative z-10 modal" @close="close">
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
              class="w-full max-w-xl transform overflow-hidden rounded bg-white p-6 text-left align-middle shadow-xl transition-all"
            >
              <DialogTitle
                as="h3"
                class="text-lg font-medium leading-6 text-gray-900 flex justify-between"
              >
                <div>
                  <slot name="header" />
                </div>
                <button :aria-label="$t('term.close')" @click="close">
                  <span aria-hidden="true">&times;</span>
                </button>
              </DialogTitle>
              <div class="mt-2 mb-2 max-h-96 overflow-auto">
                <slot name="body" />
              </div>

              <div v-if="'footer' in slots" class="mt-4 gap-1 flex justify-end">
                <slot name="footer" />
                <sba-button
                  v-if="!('footer' in slots)"
                  aria-label="close"
                  @click="close"
                >
                  {{ $t('term.close') }}
                </sba-button>
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
import { useSlots } from 'vue';

export default {
  name: 'SbaModal',
  components: {
    Dialog,
    DialogPanel,
    DialogTitle,
    TransitionRoot,
    TransitionChild,
  },
  props: {
    modelValue: { type: Boolean, default: false },
  },
  emits: ['update:modelValue'],
  setup() {
    const slots = useSlots();

    return {
      slots,
    };
  },
  methods: {
    close() {
      this.$emit('update:modelValue', false);
    },
  },
};
</script>
