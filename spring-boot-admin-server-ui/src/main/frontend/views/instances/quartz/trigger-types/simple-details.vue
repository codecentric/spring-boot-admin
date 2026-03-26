<template>
  <div class="grid grid-cols-3 gap-6 text-sm">
    <div>
      <p class="font-medium text-gray-700">Interval</p>
      <p class="mt-1 text-gray-600">
        {{ formatInterval(trigger.simple.interval) }}
      </p>
    </div>
    <div>
      <p class="font-medium text-gray-700">Repeat Count</p>
      <p class="mt-1 text-gray-600">
        {{
          trigger.simple.repeatCount === -1
            ? 'Infinite'
            : trigger.simple.repeatCount
        }}
      </p>
    </div>
    <div>
      <p class="font-medium text-gray-700">Times Triggered</p>
      <p class="mt-1 text-gray-600">{{ trigger.simple.timesTriggered }}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SimpleTriggerDetails',
  props: {
    trigger: {
      type: Object,
      required: true,
    },
  },
  methods: {
    formatInterval(milliseconds) {
      const seconds = milliseconds / 1000;
      if (seconds < 60) return `${seconds} seconds`;
      const minutes = seconds / 60;
      if (minutes < 60) return `${minutes.toFixed(1)} minutes`;
      const hours = minutes / 60;
      return `${hours.toFixed(1)} hours`;
    },
  },
};
</script>
