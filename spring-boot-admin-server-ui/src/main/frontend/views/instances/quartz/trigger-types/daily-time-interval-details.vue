<template>
  <div class="space-y-6">
    <div class="grid grid-cols-4 gap-6 text-sm">
      <div>
        <p class="font-medium text-gray-700">Interval</p>
        <p class="mt-1 text-gray-600">
          {{ formatInterval(trigger.dailyTimeInterval.interval) }}
        </p>
      </div>
      <div>
        <p class="font-medium text-gray-700">Days of Week</p>
        <p class="mt-1 text-gray-600">
          {{ formatDaysOfWeek(trigger.dailyTimeInterval.daysOfWeek) }}
        </p>
      </div>
      <div>
        <p class="font-medium text-gray-700">Start Time</p>
        <p class="mt-1 text-gray-600">
          {{ trigger.dailyTimeInterval.startTimeOfDay || '—' }}
        </p>
      </div>
      <div>
        <p class="font-medium text-gray-700">End Time</p>
        <p class="mt-1 text-gray-600">
          {{ trigger.dailyTimeInterval.endTimeOfDay || '—' }}
        </p>
      </div>
    </div>
    <div class="grid grid-cols-2 gap-6 text-sm">
      <div>
        <p class="font-medium text-gray-700">Repeat Count</p>
        <p class="mt-1 text-gray-600">
          {{
            trigger.dailyTimeInterval.repeatCount === -1
              ? 'Infinite'
              : trigger.dailyTimeInterval.repeatCount
          }}
        </p>
      </div>
      <div>
        <p class="font-medium text-gray-700">Times Triggered</p>
        <p class="mt-1 text-gray-600">
          {{ trigger.dailyTimeInterval.timesTriggered }}
        </p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DailyTimeIntervalDetails',
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
    formatDaysOfWeek(days) {
      const dayNames = {
        1: 'Sunday',
        2: 'Monday',
        3: 'Tuesday',
        4: 'Wednesday',
        5: 'Thursday',
        6: 'Friday',
        7: 'Saturday',
      };
      return days.map((d) => dayNames[d] || d).join(', ');
    },
  },
};
</script>
