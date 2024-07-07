<template>
  <div ref="treeContainer" class="x-scroller"></div>
</template>

<script lang="ts">
import { debounce } from 'lodash-es';
import { ref } from 'vue';

import Instance from '@/services/instance';
import {
  DependencyTreeData,
  createDependencyTree,
} from '@/views/instances/sbomdependencytrees/dependencyTree';

export default {
  name: 'TreeGraph',
  props: {
    sbomId: {
      type: String,
      required: true,
    },
    instance: {
      type: Instance,
      required: true,
    },
    filter: {
      type: String,
      default: '',
    },
  },
  setup() {
    const treeContainer = ref(null);

    return {
      treeContainer,
    };
  },
  data: () => ({
    error: null,
    dependencies: [],
    rootNode: null,
  }),
  watch: {
    filter: {
      deep: true,
      handler: debounce(function () {
        this.renderTree(this.dependencies);
      }, 1000),
    },
  },
  created() {
    this.fetchSbomDependencies(this.sbomId);
  },
  methods: {
    async fetchSbomDependencies(sbomId) {
      this.error = null;
      try {
        const res = await this.instance.fetchSbom(sbomId);
        this.dependencies = res.data.dependencies;
        this.renderTree(this.dependencies);
      } catch (error) {
        console.warn('Fetching sbom failed:', error);
        this.error = error;
      }
    },
    normalizeNodeName(name) {
      return name.replace(/^[^\/]*\//, '').replace(/\?.*$/, '');
    },
    normalizeData(sbomDependencies): DependencyTreeData {
      const rootNode = {
        name: `SBOM id '${this.sbomId}'`,
        dependsOn: [sbomDependencies[0]],
      };

      const children = rootNode.dependsOn.map((item) => ({
        name: this.normalizeNodeName(item.ref),
        children: this.retrieveChildren(item.dependsOn),
      }));

      return {
        name: rootNode.name,
        children,
      };
    },
    retrieveChildren(dependsOn) {
      if (!dependsOn.length) {
        return undefined;
      }

      return dependsOn.map((item) => ({
        name: this.normalizeNodeName(item),
        children: this.retrieveChildren(
          this.dependencies
            .filter((node) => {
              return node.ref === item && node.dependsOn.length;
            })
            .flatMap((node) => node.dependsOn),
        ),
      }));
    },
    renderTree(
      sbomDependencies,
      initFolding = this.filter.trim().length === 0,
    ) {
      const treeData: DependencyTreeData = this.normalizeData(sbomDependencies);

      this.rootNode = createDependencyTree(
        this.treeContainer,
        this.filterTree(treeData),
        initFolding,
      );
    },
    filterTree(treeData: DependencyTreeData): DependencyTreeData | null {
      if (!this.filter || this.filter.trim() === '') {
        return treeData;
      }

      const filterLowerCase = this.filter.trim().toLowerCase();
      const matchesCurrentNode = treeData.name
        .toLowerCase()
        .includes(filterLowerCase);

      // Recursively filter the children
      const filteredChildren = treeData.children
        ?.map((child) => this.filterTree(child))
        .filter((child) => child !== null) as DependencyTreeData[];

      if (
        matchesCurrentNode ||
        (filteredChildren && filteredChildren.length > 0)
      ) {
        return {
          ...treeData,
          children: filteredChildren,
        };
      }

      return null;
    },
    nodeContainsAnyChildWithName(child, name) {
      if (child.children) {
        return child.children.some((child) =>
          this.nodeContainsAnyChildWithName(child, name),
        );
      }
      return child.name.toLowerCase().includes(name);
    },
  },
};
</script>

<style scoped>
.x-scroller {
  overflow-x: scroll;
}
</style>
