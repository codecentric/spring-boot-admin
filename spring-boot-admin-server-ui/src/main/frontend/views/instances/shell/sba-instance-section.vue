<template>
  <section :class="{'loading': loading}">
    <slot name="before" />

    <div class="px-2 md:px-6 py-6">
      <sba-alert
        v-if="error"
        class="mb-6"
        :error="error"
        :title="$t('term.fetch_failed')"
      />

      <slot v-if="!loading" />
      <sba-loading-spinner :loading="loading" />
    </div>
  </section>
</template>

<script>
import SbaLoadingSpinner from "../../../components/sba-loading-spinner.vue";

export default {
  name: 'SbaInstanceSection',
  components: {SbaLoadingSpinner},
  props: {
    loading: {
      type: Boolean,
      default: false
    },
    error: {
      type: Error,
      default: null
    }
  }
}
</script>

