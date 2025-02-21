<template>
  <div class="flex items-center">
    <label>
      <input
        :id="id"
        :checked="modelValue"
        type="checkbox"
        class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded"
        @change="$emit('update:modelValue', $event.target.checked)"
      />
      <span class="ml-2 font-medium text-gray-700">{{ label }}</span>
    </label>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: boolean;
  label: string;
}>();

defineEmits(['update:modelValue']);
</script>
