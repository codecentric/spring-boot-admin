<template>
  <div v-if="open" class="modal is-active">
    <div class="modal-background" />

    <div class="modal-card">
      <header class="modal-card-head" v-if="$slots.header">
        <p class="modal-card-title">
          <slot name="header" />
        </p>
        <button class="delete" aria-label="close" @click="close" />
      </header>
      <section class="modal-card-body">
        <slot name="default" />
      </section>
      <footer class="modal-card-foot" v-if="$slots.footer">
        <slot name="footer" />
      </footer>
    </div>
    <button class="modal-close is-large" aria-label="close" @click="close" v-if="!$slots.header" />
  </div>
</template>

<script>
export default {
  name: 'SbaModal',
  model: {
    prop: 'open',
    event: 'change'
  },
  props: {
    open: {
      type: Boolean,
      required: true
    }
  },
  methods: {
    close() {
      this.$emit('close');
    }
  }
}
</script>

<style scoped>

</style>
