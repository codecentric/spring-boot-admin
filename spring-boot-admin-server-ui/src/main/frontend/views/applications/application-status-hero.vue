<template>
  <sba-panel>
    <template v-if="applicationsCount > 0">
      <div class="flex flex-row md:flex-col items-center justify-center">
        <template v-if="downCount === 0">
          <font-awesome-icon icon="check-circle" class="text-green-500 icon" />
          <div class="text-center">
            <h1 class="font-bold text-2xl" v-text="$t('applications.all_up')" />
            <p class="text-gray-400" v-text="lastUpdate" />
          </div>
        </template>
        <template v-else>
          <font-awesome-icon icon="minus-circle" class="text-red-500 icon" />
          <div class="text-center">
            <h1
              class="font-bold text-2xl"
              v-text="$t('applications.instances_down')"
            />
            <p class="text-gray-400" v-text="lastUpdate" />
          </div>
        </template>
      </div>
    </template>
    <template v-else>
      <div class="flex flex-col items-center">
        <font-awesome-icon
          icon="frown-open"
          class="text-gray-500 text-9xl pb-4"
        />
        <h1
          class="font-bold text-2xl"
          v-text="$t('applications.no_applications_registered')"
        />
      </div>
    </template>
  </sba-panel>
</template>

<script>
import { useApplicationStore } from '@/composables/useApplicationStore';

const options = {
  year: 'numeric',
  month: 'numeric',
  day: 'numeric',
  hour: 'numeric',
  minute: 'numeric',
  second: 'numeric',
};

export default {
  name: 'ApplicationStatusHero',
  setup() {
    const { applications } = useApplicationStore();
    return { applications };
  },
  data() {
    return {
      lastUpdate: new Date(),
      dateTimeFormat: new Intl.DateTimeFormat(this.$i18n.locale, options),
    };
  },
  computed: {
    downCount() {
      return this.applications.reduce((current, next) => {
        return (
          current +
          next.instances.filter(
            (instance) => instance.statusInfo.status !== 'UP'
          ).length
        );
      }, 0);
    },
    applicationsCount() {
      return this.applications.length;
    },
    instancesCount() {
      return this.applications.reduce(
        (current, next) => current + next.instances.length,
        0
      );
    },
  },
  watch: {
    downCount() {
      this.updateLastUpdateTime();
    },
  },
  beforeMount() {
    this.updateLastUpdateTime();
  },
  methods: {
    updateLastUpdateTime() {
      this.lastUpdate = this.dateTimeFormat.format(new Date());
    },
  },
};
</script>

<style scoped>
.icon {
  @apply text-9xl pr-4 md:pb-4 md:pr-0;
}
</style>
