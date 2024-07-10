<template>
  <div ref="treeContainer" class="x-scroller"></div>
</template>

<script lang="ts">
import { ref } from 'vue';

import Instance from '@/services/instance';
import {
  DependencyTreeData,
  createDependencyTree,
  rerenderDependencyTree,
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
    filter: 'debounceRerender',
  },
  created() {
    this.fetchSbomDependencies(this.sbomId);
  },
  methods: {
    debounceRerender(newVal) {
      this.debounce(function () {
        if (!newVal.trim()) {
          this.renderTree(this.dependencies);
        } else {
          this.updateTree();
        }
      }, 1000);
    },
    // lodash debounce won't work with multiple invocations of the same function on different dependencyTrees
    debounce(func, wait) {
      let timeout;
      // eslint-disable-next-line @typescript-eslint/no-this-alias
      const context = this,
        args = arguments;
      clearTimeout(timeout);
      timeout = setTimeout(function () {
        timeout = null;
        func.apply(context, args);
      }, wait);
    },
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
    renderTree(sbomDependencies) {
      const treeData: DependencyTreeData = this.normalizeData(sbomDependencies);

      this.rootNode = createDependencyTree(
        this.treeContainer,
        this.filterTree(treeData),
        !this.filter.trim(),
      );
    },
    updateTree() {
      rerenderDependencyTree(
        this.rootNode,
        this.filterTree(this.normalizeData(this.dependencies)),
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
