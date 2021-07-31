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
  INFO: 'INFO',
  SUCCESS: 'SUCCESS',
};

export default {
  name: 'SbaAlert',
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
          'is-danger': this.severity.toUpperCase() === Severity.ERROR,
          'is-warning': this.severity.toUpperCase() === Severity.WARN,
          'is-info': this.severity.toUpperCase() === Severity.INFO,
          'is-success': this.severity.toUpperCase() === Severity.SUCCESS,
      },
      iconClass: {
        'has-text-danger': this.severity.toUpperCase() === Severity.ERROR,
        'has-text-warning': this.severity.toUpperCase() === Severity.WARN
      }
    }
  },
}
</script>
