<template>
  <div class="message" :class="alertClass" role="alert">
    <div class="message-body">
      <strong>
        <font-awesome-icon :class="iconClass" icon="exclamation-triangle" />&nbsp;<span v-text="title" />
      </strong>
      <p v-if="error.message" v-text="error.message" />
    </div>
  </div>
</template>

<script>
export const Severity = {
  ERROR: 'ERROR',
  WARN: 'WARN',
};

export default {
  name: 'SbaError',
  props: {
    title: {
      type: String,
      required: true
    },
    error: {
      type: Error,
      default: null
    },
    severity: {
      type: String,
      default: 'ERROR'
    }
  },
  data() {
    return {
      alertClass: {
          'is-danger': this.severity === Severity.ERROR,
          'is-warning': this.severity === Severity.WARN
      },
      iconClass: {
        'has-text-danger': this.severity === Severity.ERROR,
        'has-text-warning': this.severity === Severity.WARN
      }
    }
  },
}
</script>
