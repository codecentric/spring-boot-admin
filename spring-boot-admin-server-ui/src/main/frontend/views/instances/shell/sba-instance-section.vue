<template>
  <section :class="{ loading: loading }" class="relative">
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

      <div
        v-if="loading"
        class="loading-spinner-wrapper"
        data-testid="instance-section-loading-spinner"
      >
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
  methods: {
    classNames: classNames,
  },
};
</script>

<style scoped>
.loading-spinner-wrapper {
  @apply absolute w-full h-screen flex flex-col z-50 top-0 left-0 justify-center items-center opacity-0;

  animation-name: show;
  animation-duration: 0ms;
  animation-fill-mode: forwards;
  animation-delay: 250ms;
  animation-iteration-count: 1;
}

.loading-spinner-wrapper-container {
  @apply rounded-md bg-black/30 py-4 px-5 flex w-auto gap-4 items-center text-white;
}

@keyframes show {
  from {
    @apply opacity-0;
  }

  to {
    @apply opacity-100;
  }
}
</style>
