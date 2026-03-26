<template>
  <section :class="{ 'is-loading': !hasLoaded }" class="section">
    <template v-if="hasLoaded">
      <div v-if="error" class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon
              class="has-text-danger"
              icon="exclamation-triangle"
            />
            <span v-text="$t('term.fetch_failed')" />
          </strong>
          <p v-text="error.message" />
        </div>
      </div>
      <div v-else-if="!hasJobs" class="message is-warning">
        <div class="message-body" v-text="$t('instances.quartz.no_data')" />
      </div>

      <!-- CONTENT -->
      <h1>Jobs</h1>
      <table class="table is-fullwidth">
        <thead>
          <tr>
            <th>Job Name</th>
            <th>Description</th>
            <th>Group</th>
            <th>Durable</th>
            <th>Request Recovery</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="jobDetail in jobDetails"
            :key="jobDetail.group + '-' + jobDetail.name"
          >
            <td>
              <span v-text="jobDetail.name" />
              <small v-text="'(' + jobDetail.className + ')'" />
            </td>
            <td v-text="jobDetail.description" />
            <td v-text="jobDetail.group" />
            <td v-text="jobDetail.durable" />
            <td v-text="jobDetail.requestRecovery" />
          </tr>
        </tbody>
      </table>

      <h1>Triggers</h1>
      <table class="table is-fullwidth">
        <thead>
          <tr>
            <th>Name</th>
            <th>Description</th>
            <th>Group</th>
            <th>State</th>
            <th>Type</th>
            <th>Priority</th>
            <th>Start Time</th>
            <th>End Time</th>
            <th>Previous Fire Time</th>
            <th>Next Fire Time</th>
          </tr>
        </thead>

        <trigger-row
          v-for="triggerDetail in triggerDetails"
          :key="triggerDetail.group + '-' + triggerDetail.name"
          :trigger-detail="triggerDetail"
        />
      </table>
    </template>
  </section>
</template>

<script>
import Instance from '@/services/instance';
import { QuartzActuatorService } from '@/services/quartz-actuator';
import '@/views/ViewGroup';
import { VIEW_GROUP } from '@/views/ViewGroup';
import TriggerRow from '@/views/instances/quartz/trigger-row';

export default {
  components: { TriggerRow },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data() {
    return {
      jobDetails: {},
      triggerDetails: {},
      error: null,
      hasLoaded: false,
    };
  },
  computed: {
    hasJobs() {
      return Object.keys(this.jobDetails).length >= 0;
    },
  },
  async created() {
    return this.loadData();
  },
  methods: {
    async loadData() {
      this.hasLoaded = false;
      this.error = null;
      try {
        // Load jobs and triggers in parallel
        const [jobsResults, triggersResults] = await Promise.all([
          Promise.allSettled([
            QuartzActuatorService.fetchAllJobs(this.instance),
          ]),
          Promise.allSettled([
            QuartzActuatorService.fetchAllTriggers(this.instance),
          ]),
        ]);

        // Extract successful results
        this.jobDetails = jobsResults
          .filter((r) => r.status === 'fulfilled')
          .map((r) => r.value);

        this.triggerDetails = triggersResults
          .filter((r) => r.status === 'fulfilled')
          .map((r) => r.value);
      } catch (error) {
        console.warn('Fetching Quartz data failed:', error);
        this.error = error;
      } finally {
        this.hasLoaded = true;
      }
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/quartz',
      parent: 'instances',
      path: 'quartz',
      component: this,
      label: 'instances.quartz.label',
      group: VIEW_GROUP.INSIGHTS,
      order: 50,
      isEnabled: ({ instance }) => instance.hasEndpoint('quartz'),
    });
  },
};
</script>
