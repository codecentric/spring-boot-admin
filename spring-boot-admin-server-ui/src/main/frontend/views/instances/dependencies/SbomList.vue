<template>

  <sba-instance-section :error="error" :loading="!hasLoaded">
    <sba-panel
      :key="sbomId"
      :title="sbomId"
    >
      <div class="-mx-4 -my-3">
        <table class="table table-full table-sm">
          <thead>
          <tr>
            <th>{{ $t('instances.dependencies.list.header.group') }}</th>
            <th>{{ $t('instances.dependencies.list.header.name') }}</th>
            <th>{{ $t('instances.dependencies.list.header.version') }}</th>
            <th>{{ $t('instances.dependencies.list.header.publisher') }}</th>
            <th>{{ $t('instances.dependencies.list.header.description') }}</th>
            <th>{{ $t('instances.dependencies.list.header.licenses') }}</th>
          </tr>
          </thead>
          <tbody>
          <template v-for="component in sbom.components">
            <tr>
              <td>{{ component.group }}</td>
              <td>{{ component.name }}</td>
              <td>{{ component.version }}</td>
              <td class="is-breakable">{{ component.publisher }}</td>
              <td class="is-breakable">{{ component.description }}</td>
              <td class="is-breakable">
                <ul>
                  <template v-for="licenseItem in component.licenses">
                    <li>{{licenseItem.license.id}}</li>
                  </template>
                </ul>
              </td>
            </tr>
          </template>
          </tbody>
        </table>
      </div>
    </sba-panel>
  </sba-instance-section>
</template>
<script>
import Instance from "@/services/instance";
import SbaInstanceSection from "@/views/instances/shell/sba-instance-section.vue";
import TreeTable from "@/views/instances/startup/tree-table.vue";
import SbaKeyValueTable from "@/components/sba-key-value-table.vue";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";

export default {
  name: 'sbom-list',
  components: {FontAwesomeIcon, SbaKeyValueTable, TreeTable, SbaInstanceSection},
  props: {
    instance: {
      type: Instance,
      required: true,
    },
    sbomId: {
      type: String,
      required: true,
    }
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    sbom: {},
  }),
  created() {
    this.fetchSbom(this.sbomId);
  },
  methods: {
    async fetchSbom(sbomId) {
      this.error = null;
      try {
        const res = await this.instance.fetchSbom(sbomId);
        this.sbom = res.data;
      } catch (error) {
        console.warn('Fetching sbom failed:', error);
        this.error = error;
      }
      this.hasLoaded = true;
    }
  },
}
</script>
