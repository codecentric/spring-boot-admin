<template>
  <sba-instance-section :error="error" :loading="!hasLoaded">
    <template v-for="sbom in sboms" :key="sbom">
      <sba-panel :seamless="true" :title="sbom">
        <!-- TODO: Add visualization for dependencies -->
      </sba-panel>
    </template>
  </sba-instance-section>
</template>
<script>
import Application from "@/services/application";
import Instance from "@/services/instance";
import SbaInstanceSection from "@/views/instances/shell/sba-instance-section.vue";
import {VIEW_GROUP} from "@/views/ViewGroup";
import SbaPanel from "@/components/sba-panel.vue";

export default {
  components: {
    SbaPanel,
    SbaInstanceSection
  },
  props: {
    application: {
      type: Application,
      default: () => ({}),
    },
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
    this.fetchSboms();
  },
  methods: {
    async fetchSboms() {
      this.error = null;
      try {
        const res = await this.instance.fetchSboms();
        this.sboms = res.data.ids;
      } catch (error) {
        console.warn('Fetching mappings failed:', error);
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
      label: 'instances.sboms.label',
      group: VIEW_GROUP.INSIGHTS,
      component: this,
      isEnabled: ({ instance }) => instance.hasEndpoint('sbom'),
    });
  },
};
</script>
