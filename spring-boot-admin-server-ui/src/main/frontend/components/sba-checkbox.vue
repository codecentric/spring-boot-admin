<template>
  <div class="flex items-center">
    <input
      :id="id"
      :checked="modelValue"
      type="checkbox"
      class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded"
      :name="name"
      @change="$emit('update:modelValue', $event.target.checked)"
    />
    <label :for="id" class="ml-2 font-medium text-gray-700" v-text="label" />
  </div>
</template>

<script>
export default {
  props: {
    name: {
      type: String,
      required: false,
      default: null,
    },
    modelValue: {
      type: Boolean,
      required: true,
    },
    label: {
      type: String,
      required: true,
    },
  },
  emits: ['update:modelValue'],
  computed: {
    id() {
      return 'checkbox-' + this._.uid;
    },
  },
};
</script>
