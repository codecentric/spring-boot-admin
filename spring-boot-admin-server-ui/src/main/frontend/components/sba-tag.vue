<template>
  <span
    :class="{
      'py-1 border border-gray-300 bg-white': !small,
      'py-1 border text-xs bg-white': small,
    }"
    class="inline-flex leading-none rounded shadow-sm text-sm overflow-hidden"
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
