<template>
  <div class="space-y-6">
    <div class="grid grid-cols-4 gap-6 text-sm">
      <div>
        <p class="font-medium text-gray-700">Interval</p>
        <p class="mt-1 text-gray-600">
          {{ formatInterval(trigger.calendarInterval.interval) }}
        </p>
      </div>
      <div>
        <p class="font-medium text-gray-700">Time Zone</p>
        <p class="mt-1 text-gray-600">
          {{ trigger.calendarInterval.timeZone || 'System Default' }}
        </p>
      </div>
      <div>
        <p class="font-medium text-gray-700">Times Triggered</p>
        <p class="mt-1 text-gray-600">
          {{ trigger.calendarInterval.timesTriggered }}
        </p>
      </div>
      <div>
        <p class="font-medium text-gray-700">Preserve Hour of Day</p>
        <p class="mt-1 flex items-center gap-2">
          <font-awesome-icon
            :icon="
              trigger.calendarInterval.preserveHourOfDayAcrossDaylightSavings
                ? 'check'
                : 'times'
            "
            class="h-4 w-4"
          />
        </p>
      </div>
    </div>
    <div class="text-sm">
      <p class="font-medium text-gray-700">Skip Day If Hour Does Not Exist</p>
      <p class="mt-1 flex items-center gap-2">
        <font-awesome-icon
          :icon="
            trigger.calendarInterval.skipDayIfHourDoesNotExist
              ? 'check'
              : 'times'
          "
          class="h-4 w-4"
        />
      </p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CalendarIntervalDetails',
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
      if (hours < 24) return `${hours.toFixed(1)} hours`;
      const days = hours / 24;
      return `${days.toFixed(1)} days`;
    },
  },
};
</script>
