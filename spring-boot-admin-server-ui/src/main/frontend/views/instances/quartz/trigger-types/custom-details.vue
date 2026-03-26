<template>
  <div class="text-sm">
    <p class="font-medium text-gray-700">Custom Trigger</p>
    <p class="mt-1 font-mono bg-gray-100 rounded p-2 text-gray-900 break-all">
      {{ trigger.custom.trigger }}
    </p>
  </div>
</template>

<script>
export default {
  name: 'CustomTriggerDetails',
  props: {
    trigger: {
      type: Object,
      required: true,
    },
  },
};
</script>
