<template>
  <div class="flex gap-1 w-full justify-end">
    <sba-button primary @click="() => close(true)" v-text="labelOk" />
    <sba-button
      class="button"
      @click="() => close(false)"
      v-text="labelCancel"
    />
  </div>
</template>

<script>
import SbaButton from '@/components/sba-button';

import eventBus from '@/services/bus';

export default {
  components: { SbaButton },
  props: {
    labelCancel: {
      type: String,
      required: true,
    },
    labelOk: {
      type: String,
      required: true,
    },
  },
  methods: {
    close(result) {
      eventBus.emit('sba-modal-close', result);
    },
  },
};
</script>
