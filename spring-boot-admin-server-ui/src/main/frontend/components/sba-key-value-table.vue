<template>
  <dl
    v-for="(value, key, index) in map"
    :key="key"
    class="px-4 py-3 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
    :class="{'bg-white': index%2===0, 'bg-gray-50': index%2!==0}"
  >
    <dt
      class="text-sm font-medium text-gray-500"
      v-text="getLabel(key, value)"
    />
    <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
      <slot
        :name="getSlotName(key, value)"
        :value="getValue(value)"
      >
        <sba-formatted-obj :value="getValue(value)" />
      </slot>
    </dd>
  </dl>
</template>

<script>
export default {
  name: 'SbaKeyValueTable',
  props: {
    map: {
      type: Object,
      required: true
    }
  },
  methods: {
    getSlotName(key, value) {
      return value?.id || key.replace(/[^a-zA-Z]/ig,'').toLowerCase();
    },
    getLabel(key, value) {
      return value?.label || key;
    },
    getValue(value) {
      return value?.value || value;
    }
  }
}
</script>

