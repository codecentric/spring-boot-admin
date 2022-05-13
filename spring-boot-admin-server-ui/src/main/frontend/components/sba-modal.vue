<template>
  <div
    v-if="modelValue"
    class="modal is-active"
    role="dialog"
  >
    <div class="modal-background" />

    <div class="modal-card">
      <header
        class="modal-card-head"
      >
        <p class="modal-card-title">
          <slot name="header" />
        </p>
        <button
          class="delete"
          aria-label="close"
          @click="close"
        />
      </header>
      <section
        class="modal-card-body"
        :class="{'border-rounded-top': !('header' in $slots), 'border-rounded-bottom': !('footer' in $slots)}"
      >
        <slot name="body" />
      </section>
      <footer
        v-if="'footer' in $slots"
        class="modal-card-foot"
      >
        <slot name="footer" />
      </footer>
    </div>
    <button
      v-if="!'header' in $slots"
      class="modal-close is-large"
      aria-label="close"
      @click="close"
    />
  </div>
</template>

<script>
export default {
  name: 'SbaModal',
  props: {
    modelValue: Boolean
  },
  emits: ['update:modelValue'],
  methods: {
    close() {
      this.$emit('update:modelValue', false);
    }
  }
}
</script>

<style scoped>
.modal {
  align-items: center;
  display: none;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
  position: fixed;
  z-index: 40;

  bottom: 0;
  left: 0;
  right: 0;
  top: 0;
}
.modal.is-active {
  display: flex;
}
.modal-background {
  background-color: rgba(10,10,10,.86);
  bottom: 0;
  left: 0;
  position: absolute;
  right: 0;
  top: 0;
}
.modal-card {
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 40px);
  overflow: hidden;
  margin: 0 auto;
  width: 640px;
  position: relative;
}
.modal-card-head {
  border-bottom: 1px solid #dbdbdb;
  border-top-left-radius: 6px;
  border-top-right-radius: 6px;
}
.border-rounded-top {
  border-top-left-radius: 6px;
  border-top-right-radius: 6px;
}
.border-rounded-bottom {
  border-bottom-left-radius: 6px;
  border-bottom-right-radius: 6px;
}
.modal-card-body {
  background-color: #fff;
  flex-grow: 1;
  flex-shrink: 1;
  overflow: auto;
  padding: 20px;
}
.modal-card-foot {
  border-bottom-left-radius: 6px;
  border-bottom-right-radius: 6px;
  border-top: 1px solid #dbdbdb;
}
.modal-card-foot, .modal-card-head {
  align-items: center;
  background-color: #f5f5f5;
  display: flex;
  flex-shrink: 0;
  justify-content: flex-start;
  padding: 20px;
  position: relative;
}
</style>
