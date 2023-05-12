<template>
  <section :class="{ loading: showLoadingSpinner }">
    <slot name="before" />

    <div
      :class="
        classNames({
          'flex-1': true,
          flex: layoutOptions.isFlex,
          'px-2 md:px-6 py-6': !layoutOptions.noMargin,
        })
      "
    >
      <sba-alert
        v-if="error"
        :class="
          classNames({
            'p-4': layoutOptions.noMargin,
          })
        "
        :error="error"
        :title="$t('term.fetch_failed')"
        class="mb-6 w-full"
      />

      <div v-if="showLoadingSpinner" class="loading-spinner-wrapper">
        <div class="loading-spinner-wrapper-container">
          <sba-loading-spinner size="sm" />
          {{ $t('term.fetching_data') }}
        </div>
      </div>

      <slot />
    </div>
  </section>
</template>

<script>
import classNames from 'classnames';

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
    layoutOptions: {
      type: Object,
      default: () => ({
        flex: false,
        noMargin: false,
      }),
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
  methods: {
    classNames: classNames,
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
