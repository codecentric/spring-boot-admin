<template>

  <sba-instance-section :error="error" :loading="!hasLoaded">
    <sba-panel :header-sticks-below="'#subnavigation'" :title="`${sbomId} (${filterResultString})`" :key="sbomId">
      <div class="-mx-4 -my-3">
        <table class="table table-full table-sm">
          <thead>
          <tr>
            <th>{{ $t('instances.dependencies.list.header.group') }}</th>
            <th>{{ $t('instances.dependencies.list.header.name') }}</th>
            <th>{{ $t('instances.dependencies.list.header.version') }}</th>
            <th>{{ $t('instances.dependencies.list.header.licenses') }}</th>
            <th>{{ $t('instances.dependencies.list.header.publisher') }}</th>
            <th>{{ $t('instances.dependencies.list.header.description') }}</th>
          </tr>
          </thead>
          <tbody>
          <template v-for="component in filteredComponents">
            <tr>
              <td>{{ component.group }}</td>
              <td>{{ component.name }}</td>
              <td>{{ component.version }}</td>
              <td class="is-breakable">
                <ul>
                  <template v-for="licenseItem in component.licenses">
                    <li>{{ licenseItem.license.id }}</li>
                  </template>
                </ul>
              </td>
              <td class="is-breakable">{{ component.publisher }}</td>
              <td class="is-breakable">{{ component.description }}</td>
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
import SbaStickySubnav from "@/components/sba-sticky-subnav.vue";
import {compareBy} from "@/utils/collections";

export default {
  name: 'sbom-list',
  components: {SbaStickySubnav, FontAwesomeIcon, SbaKeyValueTable, TreeTable, SbaInstanceSection},
  props: {
    instance: {
      type: Instance,
      required: true,
    },
    sbomId: {
      type: String,
      required: true,
    },
    filter: {
      type: String,
      required: true,
    }
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    components: [],
  }),
  computed: {
    filterResultString() {
      return `${this.filteredComponents.length}/${this.components.length}`;
    },
    filteredComponents() {
      const filterFn = this.getFilterFn();
      return this.components.filter(filterFn)
        .sort(compareBy((component) => `${component.group}:${component.name}:${component.version}`));
    },
  },
  created() {
    this.fetchSbom(this.sbomId);
  },
  methods: {
    getFilterFn() {
      if (!this.filter) {
        return () => true;
      }
      const regex = new RegExp(this.filter, 'i');
      return (component) =>
        (component.name && component.name.match(regex))
        || (component.group && component.group.match(regex))
        || (component.version && component.version.match(regex))
        || (component.publisher && component.publisher.match(regex))
        || (component.licenses && component.licenses.some((license) => (license.license.id && license.license.id.match(regex))));
    },
    async fetchSbom(sbomId) {
      this.error = null;
      try {
        const res = await this.instance.fetchSbom(sbomId);
        this.components = res.data.components;
      } catch (error) {
        console.warn('Fetching sbom failed:', error);
        this.error = error;
      }
      this.hasLoaded = true;
    }
  },
}
</script>
