<template>

  <sba-instance-section :error="error" :loading="!hasLoaded">
    <sba-panel :header-sticks-below="'#subnavigation'" :title="`${sbomId} (${filterResultString})`" :key="sbomId">
      <div class="-mx-4 -my-3">
        <table class="table table-full table-sm">
          <thead>
          <tr>
            <th class="table-header-clickable" @click="toggleSort('group')">{{ $t('instances.dependencies.list.header.group') }}
              <font-awesome-icon v-if="sortedBy['group']"
                icon="chevron-down"
                :class="{
                    '-rotate-180': sortedBy['group'] === 'DESC',
                    'transition-[transform]': true,
                  }"
              />
            </th>
            <th class="table-header-clickable" @click="toggleSort('name')">{{ $t('instances.dependencies.list.header.name') }}
              <font-awesome-icon v-if="sortedBy['name']"
                                 icon="chevron-down"
                                 :class="{
                    '-rotate-180': sortedBy['name'] === 'DESC',
                    'transition-[transform]': true,
                  }"
              />
            </th>
            <th class="table-header-clickable" @click="toggleSort('version')">{{ $t('instances.dependencies.list.header.version') }}
              <font-awesome-icon v-if="sortedBy['version']"
                                 icon="chevron-down"
                                 :class="{
                    '-rotate-180': sortedBy['version'] === 'DESC',
                    'transition-[transform]': true,
                  }"
              />
            </th>
            <th>{{ $t('instances.dependencies.list.header.licenses') }}</th>
            <th class="table-header-clickable" @click="toggleSort('publisher')">{{ $t('instances.dependencies.list.header.publisher') }}
              <font-awesome-icon v-if="sortedBy['publisher']"
                                 icon="chevron-down"
                                 :class="{
                    '-rotate-180': sortedBy['publisher'] === 'DESC',
                    'transition-[transform]': true,
                  }"
              />
            </th>
            <th>{{ $t('instances.dependencies.list.header.description') }}</th>
          </tr>
          </thead>
          <tbody>
          <template v-for="component in filteredAndSortedComponents">
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
  components: {
    SbaStickySubnav,
    FontAwesomeIcon,
    SbaKeyValueTable,
    TreeTable,
    SbaInstanceSection
  },
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
    sortedBy: {
      group: 'ASC',
      name: 'ASC',
      version: 'ASC'
    }
  }),
  computed: {
    filterResultString() {
      return `${this.filteredAndSortedComponents.length}/${this.components.length}`;
    },
    filteredAndSortedComponents() {
      const filterFn = this.getFilterFn();
      return this.sortFn(this.components.filter(filterFn));
    },
  },
  created() {
    this.fetchSbom(this.sbomId);
  },
  methods: {
    sortFn(components) {
      if (Object.keys(this.sortedBy).length === 0) {
        return components;
      }

      const sortFunctions = {};
      for (const property in this.sortedBy) {
        const order = this.sortedBy[property];
        sortFunctions[property] = function (a, b) {
          if (a[property] && !b[property]) {
            return 1;
          } else if (!a[property] && b[property]) {
            return -1;
          } else if (!a[property] && !b[property]) {
            return 0;
          }

          const compareResult = a[property].localeCompare(b[property]);
          return order === 'ASC' ? compareResult : compareResult * -1;
        };
      }
      return components.sort((a, b) => {
        for (const property in sortFunctions) {
          const compareResult = sortFunctions[property](a,b);
          if (compareResult !== 0) {
            return compareResult;
          }
        }
        return 0;
      })
    },
    toggleSort(field) {
      if (this.sortedBy[field] && this.sortedBy[field] === 'DESC') {
        delete this.sortedBy[field];
      } else if (this.sortedBy[field]) {
        this.sortedBy[field] = 'DESC';
      } else {
        this.sortedBy[field] = 'ASC';
      }
    },
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
<style lang="scss" scoped>
.table-header-clickable {
  cursor: pointer;
}
</style>
