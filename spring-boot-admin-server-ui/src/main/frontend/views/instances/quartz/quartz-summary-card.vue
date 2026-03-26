<template>
  <div :class="cardClass">
    <div class="flex items-center justify-between">
      <div>
        <p :class="labelClass">{{ label }}</p>
        <p class="mt-2 text-3xl font-bold" :class="valueClass">
          {{ value }}
        </p>
      </div>
      <font-awesome-icon :icon="icon" class="h-10 w-10" :class="iconClass" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

type CardVariant = 'blue' | 'green' | 'yellow' | 'red';

interface Props {
  label: string;
  value: number;
  icon: string;
  variant?: CardVariant;
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'blue',
});

const variantMap = {
  blue: {
    card: 'bg-gradient-to-br from-blue-50 to-blue-100 border-blue-200',
    label: 'text-blue-600',
    value: 'text-blue-900',
    icon: 'text-blue-400',
  },
  green: {
    card: 'bg-gradient-to-br from-green-50 to-green-100 border-green-200',
    label: 'text-green-600',
    value: 'text-green-900',
    icon: 'text-green-400',
  },
  yellow: {
    card: 'bg-gradient-to-br from-yellow-50 to-yellow-100 border-yellow-200',
    label: 'text-yellow-600',
    value: 'text-yellow-900',
    icon: 'text-yellow-400',
  },
  red: {
    card: 'bg-gradient-to-br from-red-50 to-red-100 border-red-200',
    label: 'text-red-600',
    value: 'text-red-900',
    icon: 'text-red-400',
  },
};

const cardClass = computed(() => {
  const baseClass = 'rounded-lg p-6 border shadow-sm';
  const variant = variantMap[props.variant];
  return `${baseClass} ${variant.card}`;
});

const labelClass = computed(() => {
  const baseClass = 'text-sm font-medium uppercase tracking-wide';
  const variant = variantMap[props.variant];
  return `${baseClass} ${variant.label}`;
});

const valueClass = computed(() => variantMap[props.variant].value);

const iconClass = computed(() => variantMap[props.variant].icon);
</script>
