<template>
  <tbody>
    <tr>
      <td>
        <a class="icon" :class="{'icon--open': isOpen}" @click="toggle" role="button" />
        <span v-text="triggerDetail.name" />
      </td>
      <td v-text="triggerDetail.description" />
      <td v-text="triggerDetail.group" />
      <td v-text="triggerDetail.state" />
      <td v-text="triggerDetail.type" />
      <td v-text="triggerDetail.priority" />
      <td v-text="triggerDetail.startTime" />
      <td v-text="triggerDetail.endTime" />
      <td v-text="triggerDetail.previousFireTime" />
      <td v-text="triggerDetail.nextFireTime" />
    </tr>
    <tr v-if="isOpen">
      <td colspan="10">
        <table class="table">
          <tr v-for="(value,name) in triggerTypeDetails" :key="name">
            <td v-text="name" />
            <td v-text="value" />
          </tr>
        </table>
      </td>
    </tr>
  </tbody>
</template>

<script>

export default {
  name: 'TriggerRow',
  props: {
    triggerDetail: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      isOpen: false
    }
  },
  computed: {
    triggerTypeDetails() {
      return ['calendarInterval', 'cron', 'custom', 'dailyTimeInterval', 'simple']
        .map(type => this.triggerDetail[type])
        .find(ttd => ttd !== undefined) || {}
    }
  },
  methods: {
    toggle() {
      this.isOpen = !this.isOpen
    }
  }

}
</script>

<style lang="scss" scoped>
.icon {
  $iconSize: 6px;
  width: $iconSize * 1.2;
  height: $iconSize;

  margin-right: 10px;

  border-top: $iconSize solid transparent;
  border-left: $iconSize*1.2 solid #555;
  border-bottom: $iconSize solid transparent;

  &--open {
    transform: rotate(90deg);
  }

  &.empty {
    border: none;
  }
}
</style>
