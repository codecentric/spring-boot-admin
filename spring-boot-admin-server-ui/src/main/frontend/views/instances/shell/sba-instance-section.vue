<template>
  <section :class="{ loading: showLoadingSpinner }">
    <slot name="before" />

    <div class="px-2 md:px-6 py-6">
      <sba-alert
        v-if="error"
        class="mb-6"
        :error="error"
        :title="$t('term.fetch_failed')"
      />

      <div v-if="showLoadingSpinner" class="loading-spinner-wrapper">
        <div class="loading-spinner-wrapper-container">
          <sba-loading-spinner :loading="showLoadingSpinner" size="sm" />
          {{ $t('term.fetching_data') }}
        </div>
      </div>

      <slot />
    </div>
  </section>
</template>

<script>
import SbaLoadingSpinner from '@/components/sba-loading-spinner';

export default {
  name: 'SbaInstanceSection',
  components: { SbaLoadingSpinner },
  props: {
    loading: {
      type: Boolean,
      default: false,
    },
    error: {
      type: Error,
      default: null,
    },
  },
  data() {
    return {
      showLoadingSpinner: false,
      debouncedLoader: null,
    };
  },
  watch: {
    loading: function (newVal) {
      window.clearTimeout(this.debouncedLoader);
      this.debouncedLoader = window.setTimeout(() => {
        this.showLoadingSpinner = newVal;
      }, 250);
    },
  },
};
</script>

<style scoped>
.loading-spinner-wrapper {
  @apply w-full h-full flex flex-col bg-black/30 absolute z-50 top-0 left-0 justify-center items-center backdrop-blur-sm;
}

.loading-spinner-wrapper-container {
  @apply rounded-md bg-black/30 py-4 px-5 flex w-auto gap-4 flex items-center text-white backdrop-blur;
}
</style>
