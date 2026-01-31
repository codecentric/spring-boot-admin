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
    <template v-if="sboms.length === 0">
      <sba-alert
        severity="WARN"
        :error="$t('instances.dependencies.no_data_provided')"
      />
    </template>
    <template v-for="sbomId in sboms" v-else :key="sbomId">
      <sbom-list :instance="instance" :sbom-id="sbomId" :filter="filter" />
    </template>
  </sba-instance-section>
</template>
<script setup>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { onMounted, ref } from 'vue';

import SbaAlert from '@/components/sba-alert.vue';
import SbaStickySubnav from '@/components/sba-sticky-subnav.vue';

import Instance from '@/services/instance';
import SbomList from '@/views/instances/dependencies/SbomList.vue';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section.vue';

const props = defineProps({
  instance: {
    type: Instance,
    required: true,
  },
});

const hasLoaded = ref(false);
const error = ref(null);
const sboms = ref([]);
const filter = ref('');

const fetchSbomIds = async () => {
  error.value = null;
  try {
    const res = await props.instance.fetchSbomIds();
    sboms.value = res.data.ids;
  } catch (err) {
    console.warn('Fetching sbom ids failed:', err);
    error.value = err;
  }
  hasLoaded.value = true;
};

onMounted(() => {
  fetchSbomIds();
});
</script>

<script>
import { VIEW_GROUP } from '@/views/ViewGroup';

export default {
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
