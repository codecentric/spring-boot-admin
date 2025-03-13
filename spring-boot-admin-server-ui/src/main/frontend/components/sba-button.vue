<template>
  <button
    class="btn relative"
    :class="cssClasses"
    :disabled="disabled === true"
    :title="title"
    @click="$emit('click', $event)"
  >
    <slot />
  </button>
</template>

<script>
export default {
  name: 'SbaButton',
  props: {
    title: {
      type: String,
      default: '',
    },
    size: {
      type: String,
      default: 'sm',
      validate(value) {
        return ['xs', 'sm', 'base'].includes(value);
      },
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    primary: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['click'],
  computed: {
    cssClasses() {
      return {
        'px-2 py-2 text-xs': this.size === 'xs',
        'px-3 py-2': this.size === 'sm',
        'px-4 py-3': this.size === 'base',
        'px-5 py-2.5': this.size === '',
        // Types
        'is-primary': this.primary === true,
      };
    },
  },
};
</script>

<style scoped>
.btn {
  @apply rounded-l rounded-r font-medium text-sm text-center text-black border-gray-300 border border-gray-300 bg-white;
  @apply focus:ring-2 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500;
  @apply hover:bg-gray-100;
}

.btn:disabled {
  @apply text-gray-300;
}

.btn.is-danger {
  @apply text-white bg-red-600 hover:bg-red-700 focus:ring-red-300;
}

.btn.is-warning {
  @apply focus:outline-none text-gray-700 bg-yellow-400 hover:bg-yellow-500 focus:ring-yellow-300 font-medium text-sm;
}

.btn.is-info {
  @apply text-white bg-blue-700 hover:bg-blue-800  focus:ring-blue-300 font-medium text-sm focus:outline-none;
}

.btn.is-success {
  @apply text-white bg-green-700 hover:bg-green-800  focus:ring-green-300 font-medium text-sm focus:outline-none;
}

.btn.is-light {
  @apply text-gray-900 bg-white border border-gray-300 focus:outline-none hover:bg-gray-100  focus:ring-gray-200 font-medium  text-sm;

  &.is-active {
    @apply bg-gray-300;
  }
}

.btn.is-extra-light {
  @apply text-white bg-gray-500 hover:bg-gray-400 focus:outline-none focus:ring-gray-300 font-medium  text-sm;

  &.is-active {
    @apply bg-gray-400;
  }
}

.btn.is-black {
  @apply text-white bg-gray-800 hover:bg-gray-900 focus:outline-none focus:ring-gray-300 font-medium  text-sm;
}

.btn.is-primary {
  @apply text-white bg-blue-600 hover:bg-blue-700 focus:ring-blue-300;
}

@supports (-moz-appearance: none) {
  .backdrop-filter.bg-opacity-40 {
    --tw-bg-opacity: 1 !important;
  }
}
</style>
