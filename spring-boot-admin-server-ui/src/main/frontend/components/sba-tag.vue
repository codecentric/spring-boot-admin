<template>
  <span class="inline-flex leading-none rounded shadow-sm text-sm overflow-hidden"
        :class="{
          'p-1 border border-gray-300 bg-white': !small,
          'p-0 border text-xs': small,
         }"
  >
    <span
      v-if="label"
      class="inline-flex bg-sba-200 rounded text-sba-900  justify-center items-center transition-all"
      :class="{
         'py-1.5 px-3': !small,
         'py-1.5 px-2 rounded-r-none': small
      }"
      v-text="label"
    />
    <span
      class="inline-flex px-2 justify-center items-center whitespace-nowrap"
      :class="{
        'pl-2': !!label,
        'bg-white': small
      }"
      v-text="value"
    />
  </span>
</template>

<script>
export default {
  name: 'SbaTag',
  props: {
    label: {
      type: [String, Number],
      default: null
    },
    small: {
      type: Boolean,
      default: false
    },
    value: {
      type: [String, Number],
      required: true
    }
  }
}
</script>
