<template>
  <span
    :class="{
      'text-xs': small,
    }"
    class="inline-flex leading-none rounded text-sm overflow-hidden border border-gray-200 bg-white py-1"
  >
    <span
      v-if="label"
      :class="{
        'py-1.5 px-2 ml-1': !small,
        'py-1.5 px-1.5 ml-1': small,
      }"
      class="inline-flex bg-sba-200 rounded text-sba-900 justify-center items-center transition-all whitespace-nowrap"
      v-text="label"
    />
    <span
      :class="{
        'pl-2': !!label,
        'bg-white pl-1.5': small,
      }"
      class="inline-flex px-2 justify-center items-center whitespace-nowrap"
      v-text="value"
    />
  </span>
</template>

<script setup>
defineProps({
  label: {
    type: [String, Number],
    default: null,
  },
  small: {
    type: Boolean,
    default: false,
  },
  value: {
    type: [String, Number],
    required: true,
  },
});
</script>
