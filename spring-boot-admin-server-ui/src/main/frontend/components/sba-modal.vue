<template>
  <div v-if="open" class="modal is-active">
    <div class="modal-background" ></div>

    <div class="modal-card">
      <header class="modal-card-head">
        <p class="modal-card-title"><slot name="header"/></p>
        <button class="delete" aria-label="close" @click="close"></button>
      </header>
      <section class="modal-card-body">
        <slot name="body"/>
      </section>
      <footer class="modal-card-foot">
        <slot name="footer"/>
      </footer>
    </div>
  </div>
</template>

<script>
export default {
  name: "sba-modal",
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
      this.$emit("close");
    }
  }
}
</script>

<style scoped>

</style>
