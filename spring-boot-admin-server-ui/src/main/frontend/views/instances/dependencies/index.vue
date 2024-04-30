<template>
  <sba-instance-section :error="error" :loading="!hasLoaded">
        <template v-for="sbomId in sboms" :key="sbomId">
          <sbom-list :instance="instance" :sbomId="sbomId"/>
        </template>
  </sba-instance-section>
</template>
<script>
import Application from "@/services/application";
import Instance from "@/services/instance";
import SbaInstanceSection from "@/views/instances/shell/sba-instance-section.vue";
import {VIEW_GROUP} from "@/views/ViewGroup";
import SbomList from "@/views/instances/dependencies/SbomList.vue";

export default {
  components: {
    SbomList,
    SbaInstanceSection
  },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    sboms: [],
  }),
  created() {
    this.fetchSbomIds();
  },
  methods: {
    async fetchSbomIds() {
      this.error = null;
      try {
        const res = await this.instance.fetchSbomIds();
        this.sboms = res.data.ids;
      } catch (error) {
        console.warn('Fetching sbom ids failed:', error);
        this.error = error;
      }
      this.hasLoaded = true;
    }
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/dependencies',
      parent: 'instances',
      path: 'dependencies',
      label: 'instances.dependencies.label',
      group: VIEW_GROUP.INSIGHTS,
      component: this,
      isEnabled: ({ instance }) => instance.hasEndpoint('sbom'),
    });
  },
};
</script>
