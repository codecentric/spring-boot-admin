<template>
<div class="flex-1 px-2 md:px-6 py-6">
  <div class="shadow-sm border rounded break-inside-avoid mb-6">
    <header class="rounded-t flex justify-between px-4 pt-5 pb-5 border-b sm:px-6 items-center bg-white transition-all">
      <h3 class="text-lg leading-6 font-medium text-gray-900">Start new JVM flight recording</h3>
    </header>
    <div class="rounded-b border-gray-200 px-4 py-3 bg-white">
      <form @submit.prevent="submitEntry" class="flex items-end gap-x-4">
        <div class="flex items-center mr-4">
          <label for="duration" class="mr-2">Duration:</label>
          <input id="duration" v-model="duration" type="number" required class="w-20">
          <select v-model="timeUnit" class="ml-2">
            <option value="SECONDS">Seconds</option>
            <option value="MINUTES">Minutes</option>
            <option value="HOURS">Hours</option>
          </select>
        </div>
        <div class="flex-1 mr-4">
          <label for="description" class="sr-only">Description</label>
          <input id="description" v-model="description" type="text" required placeholder="Description..." class="w-full">
        </div>
        <sba-button type="submit" :disabled="isSubmitDisabled">
          <font-awesome-icon icon="circle" />&nbsp;Start recording
        </sba-button>
      </form>
    </div>
  </div>
  <div class="shadow-sm border rounded break-inside-avoid mb-6">
    <header class="rounded-t flex justify-between px-4 pt-5 pb-5 border-b sm:px-6 items-center bg-white transition-all">
      <h3 class="text-lg leading-6 font-medium text-gray-900">Recordings</h3>
    </header>
    <div class="rounded-b border-gray-200 px-4 py-3 bg-white">
      <table class="table-fixed w-full" v-if="data.length > 0">
        <tbody>
        <tr>
          <th class="w-1/12">ID</th>
          <th class="w-4/12">Description</th>
          <th class="w-2/12">Started at</th>
          <th class="w-2/12">Finished at</th>
          <th class="w-1/12">Status</th>
          <th class="w-2/12"></th>
        </tr>
        <tr v-for="recording in data" :key="recording.id">
          <td v-text="recording.id"/>
          <td v-text="recording.description"/>
          <td v-text="recording.startedAt" />
          <td v-text="recording.finishedAt" />
          <td v-text="recording.status"/>
          <td class="text-right">
            <sba-button @click="stopRecording(recording.id)" v-if="recording.status === 'RUNNING'">
              <font-awesome-icon icon="stop-circle" />&nbsp;Stop
            </sba-button>
            <sba-button @click="viewFlameGraph(recording.id)" v-if="recording.status === 'CLOSED'">
              <font-awesome-icon icon="eye" />&nbsp;View
            </sba-button>
            <sba-button @click="downloadRecording(recording.id)" v-if="recording.status === 'CLOSED'">
              <font-awesome-icon icon="download" />&nbsp;Download
            </sba-button>
            <sba-button @click="deleteRecording(recording.id)" v-if="recording.status === 'CLOSED'">
              <font-awesome-icon icon="trash" />&nbsp;Delete
            </sba-button>
          </td>
        </tr>
        </tbody>
      </table>
      <h4 v-else>
        No recordings found. Please start a new flight recording first.
      </h4>
    </div>
  </div>
  <div class="shadow-sm border rounded break-inside-avoid mb-6" v-if="flameGraphUrl">
    <header class="rounded-t flex justify-between px-4 pt-5 pb-5 border-b sm:px-6 items-center bg-white transition-all">
      <h3 class="text-lg leading-6 font-medium text-gray-900">Flame Graph</h3>
      <sba-button @click="closeFlamegraph()">
        <font-awesome-icon icon="times" />
      </sba-button>
    </header>
    <div class="rounded-b border-gray-200 bg-white">
      <iframe :src="flameGraphUrl" class="w-full h-screen border-0" />
    </div>
  </div>
</div>
</template>

<script>
export default {
  props: {
    instance: {
      //<1>
      type: Object,
      required: true,
    },
  },
  data: () => ({
    data: [],
    flameGraphUrl: null,
    duration: 60,
    description: '',
    timeUnit: 'SECONDS',
    intervalId: null,
  }),
  computed: {
    isSubmitDisabled() {
      return !this.duration || !this.description.trim();
    }
  },
  async created() {
    await this.fetchRecordings();
    this.intervalId = setInterval(() => this.fetchRecordings(), 10000);
  },
  beforeDestroy() {
    clearInterval(this.intervalId);
  },
  methods: {
    async fetchRecordings() {
      const response = await this.instance.axios.get("actuator/flightrecorder");
      this.data = response.data || [];
    },
    async submitEntry() {
      let data = {
        duration: this.duration,
        timeUnit: this.timeUnit,
        description: this.description,
      }
      this.description = '';
      await this.instance.axios.post('actuator/flightrecorder', data);
      await this.fetchRecordings();
    },
    downloadRecording(id) {
      window.open(`instances/${this.instance.id}/actuator/flightrecorder/` + id, '_blank');
    },
    async stopRecording(id) {
      await this.instance.axios.put(`actuator/flightrecorder/${id}`);
      await this.fetchRecordings();
    },
    async deleteRecording(id) {
      await this.instance.axios.delete(`actuator/flightrecorder/${id}`);
      await this.fetchRecordings();
    },
    viewFlameGraph(id) {
      this.flameGraphUrl = `instances/${this.instance.id}/actuator/flightrecorder/ui/${id}/flamegraph.html`;
    },
    closeFlamegraph() {
      this.flameGraphUrl = null;
    },
  },
};
</script>
