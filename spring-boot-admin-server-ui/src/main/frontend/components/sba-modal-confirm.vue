<template>
  <sba-modal v-model="isOpen">
    <template #header>
      {{ title }}
    </template>
    <template #body>
      <slot />
    </template>
    <template #footer>
      <div class="flex gap-1 w-full justify-end">
        <sba-button
          primary
          @click="$emit('close', true)"
          v-text="$t('term.ok')"
        />
        <sba-button
          v-if="!skipCancel"
          class="button"
          @click="$emit('close', false)"
          v-text="$t('term.cancel')"
        />
      </div>
    </template>
  </sba-modal>
</template>
<script>

export default {
  props: {
    open: {
      type: Boolean,
      required: true
    },
    skipCancel: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      required: true
    }
  },
  emits: ['close'],
  computed: {
    isOpen() {
      return this.open;
    }
  }
}
</script>
