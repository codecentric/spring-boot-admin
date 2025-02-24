<template>
  <sba-instance-section :error="error" :loading="!hasLoaded">
    <sba-panel
      :key="sbomId"
      :header-sticks-below="'#subnavigation'"
      :title="`${sbomId} (${filterResultString})`"
    >
      <div class="-mx-4 -my-3">
        <table class="table table-full table-sm">
          <thead data-testid="sbom-table-header">
            <tr>
              <template v-for="column in columns" :key="column.property">
                <th
                  v-if="column.sortable"
                  class="table-header-clickable"
                  @click="toggleSort(column.property)"
                >
                  {{
                    $t(`instances.dependencies.list.header.${column.property}`)
                  }}
                  <font-awesome-icon
                    v-if="sortedBy[column.property]"
                    :data-testid="`sorted-icon-${column.property}-${sortedBy[column.property]}`"
                    icon="chevron-down"
                    :class="{
                      '-rotate-180': sortedBy[column.property] === 'DESC',
                      'transition-[transform]': true,
                    }"
                  />
                </th>
                <th v-else>
                  {{
                    $t(`instances.dependencies.list.header.${column.property}`)
                  }}
                </th>
              </template>
            </tr>
          </thead>
          <tbody data-testid="sbom-table-body">
            <template
              v-for="component in filteredAndSortedComponents"
              :key="`${component.group}:${component.name}:${component.version}`"
            >
              <tr data-testid="sbom-table-body-row">
                <td>{{ component.group }}</td>
                <td>{{ component.name }}</td>
                <td>{{ component.version }}</td>
                <td>
                  <dl
                    v-if="component.licenses"
                    class="divide-y divide-gray-200"
                  >
                    <template
                      v-for="licenseItem in component.licenses
                        .filter((license) => license.license)
                        .map((license) => license.license)"
                      :key="licenseItem.id ? licenseItem.id : licenseItem.name"
                    >
                      <dt v-if="licenseItem.url">
                        <a :href="licenseItem.url">{{
                          licenseItem.id ? licenseItem.id : licenseItem.name
                        }}</a>
                      </dt>
                      <dt v-else>
                        {{ licenseItem.id ? licenseItem.id : licenseItem.name }}
                      </dt>
                    </template>
                  </dl>
                </td>
                <td>{{ component.publisher }}</td>
                <td>{{ component.description }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </sba-panel>
  </sba-instance-section>
</template>
<script>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

import Instance from '@/services/instance';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section.vue';

export default {
  name: 'SbomList',
  components: {
    FontAwesomeIcon,
    SbaInstanceSection,
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
    },
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    components: [],
    sortedBy: {
      group: 'ASC',
      name: 'ASC',
      version: 'ASC',
    },
    columns: [
      {
        property: 'group',
        sortable: true,
      },
      {
        property: 'name',
        sortable: true,
      },
      {
        property: 'version',
        sortable: true,
      },
      {
        property: 'licenses',
        sortable: false,
      },
      {
        property: 'publisher',
        sortable: true,
      },
      {
        property: 'description',
        sortable: false,
      },
    ],
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
          const compareResult = sortFunctions[property](a, b);
          if (compareResult !== 0) {
            return compareResult;
          }
        }
        return 0;
      });
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
        (component.name && component.name.match(regex)) ||
        (component.group && component.group.match(regex)) ||
        (component.version && component.version.match(regex)) ||
        (component.publisher && component.publisher.match(regex)) ||
        (component.licenses &&
          component.licenses.some(
            (license) =>
              license.license && JSON.stringify(license.license).match(regex),
          ));
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
    },
  },
};
</script>
<style lang="scss" scoped>
.table-header-clickable {
  cursor: pointer;
}

dt > a:hover {
  text-decoration: underline;
}
</style>
