<template>
  <sba-instance-section :error="error" :loading="!hasLoaded">
    <template #before>
      <sba-sticky-subnav>
        <sba-input
          v-model="filter"
          :placeholder="$t('term.filter')"
          name="filter"
          type="search"
        >
          <template #prepend>
            <font-awesome-icon icon="filter" />
          </template>
        </sba-input>
      </sba-sticky-subnav>
    </template>
    <template v-for="sbomId in sboms" :key="sbomId">
      <sbom-list :instance="instance" :sbom-id="sbomId" :filter="filter" />
    </template>
  </sba-instance-section>
</template>
<script>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

import SbaStickySubnav from '@/components/sba-sticky-subnav.vue';

import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SbomList from '@/views/instances/dependencies/SbomList.vue';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section.vue';

export default {
  components: {
    FontAwesomeIcon,
    SbaStickySubnav,
    SbomList,
    SbaInstanceSection,
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
    filter: '',
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
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/dependencies',
      parent: 'instances',
      path: 'dependencies',
      label: 'instances.dependencies.label',
      group: VIEW_GROUP.DEPENDENCIES,
      order: 1,
      component: this,
      isEnabled: ({ instance }) => instance.hasEndpoint('sbom'),
    });
  },
};
</script>
