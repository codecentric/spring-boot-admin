<template>
  <dl
    v-for="(value, key, index) in filteredMap"
    :key="key"
    class="px-4 py-3 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
    :class="{ 'bg-white': index % 2 === 0, 'bg-gray-50': index % 2 !== 0 }"
  >
    <dt
      class="text-sm font-medium text-gray-500 break-all"
      v-text="getLabel(key, value)"
    />
    <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
      <slot :name="getSlotName(key, value)" :value="getValue(value)">
        <sba-formatted-obj :value="getValue(value)" />
      </slot>
    </dd>
  </dl>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const { map, skipNullValues = false } = defineProps<{
  map: Record<string, any>;
  skipNullValues?: boolean;
}>();

const filteredMap = computed(() => {
  return Object.entries(map)
    .filter(([_, value]) => {
      if (skipNullValues) {
        return value && value.value !== null;
      }
      return true;
    })
    .reduce(
      (acc, [key, value]) => {
        acc[key] = value;
        return acc;
      },
      {} as Record<string, any>,
    );
});

const getSlotName = (key: string, value: any) => {
  return value?.id || key.replace(/[^a-zA-Z]/gi, '').toLowerCase();
};

const getLabel = (key: string, value: any) => {
  return value?.label || key;
};

const getValue = (value: any) => {
  return value?.value || value;
};
</script>
