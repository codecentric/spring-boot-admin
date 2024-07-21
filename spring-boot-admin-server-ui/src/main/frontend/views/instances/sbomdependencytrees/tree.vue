<!--
  - Copyright 2014-2024 the original author or authors.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <div ref="treeContainer" class="x-scroller"></div>
</template>

<script lang="ts">
import { onMounted, ref, watch } from 'vue';

import Instance from '@/services/instance';
import {
  DependencyTreeData,
  createDependencyTree,
  rerenderDependencyTree,
} from '@/views/instances/sbomdependencytrees/dependencyTree';

type SbomDependency = {
  ref: string;
  dependsOn?: string[];
};

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
  setup(props) {
    const treeContainer = ref<HTMLElement | null>(null);
    const dependencies = ref([]);
    const rootNode = ref(null);
    const error = ref(null);
    let timeout: number | null = null;

    const normalizeNodeName = (name: string): string =>
      name.replace(/^[^\/]*\//, '').replace(/\?.*$/, '');

    const retrieveChildren = (
      dependsOn: string[],
    ): DependencyTreeData[] | undefined => {
      if (!dependsOn.length) return undefined;

      return dependsOn.map((item) => ({
        name: normalizeNodeName(item),
        children: retrieveChildren(
          dependencies.value
            .filter((node) => node.ref === item && node.dependsOn.length)
            .flatMap((node) => node.dependsOn),
        ),
      }));
    };

    const normalizeData = (
      sbomDependencies: SbomDependency[],
    ): DependencyTreeData => {
      const rootNode = {
        name: `SBOM id '${props.sbomId}'`,
        dependsOn: [sbomDependencies[0]],
      };

      const children = rootNode.dependsOn.map((item) => ({
        name: normalizeNodeName(item.ref),
        children: retrieveChildren(item.dependsOn),
      }));

      return {
        name: rootNode.name,
        children,
      };
    };

    const fetchSbomDependencies = async (sbomId: string): Promise<void> => {
      error.value = null;
      try {
        const res = await props.instance.fetchSbom(sbomId);
        dependencies.value = res.data.dependencies;
        await renderTree(dependencies.value);
      } catch (err) {
        console.warn('Fetching sbom failed:', err);
        error.value = err;
      }
    };

    const filterTree = (
      treeData: DependencyTreeData,
    ): DependencyTreeData | null => {
      if (!props.filter || props.filter.trim() === '') {
        return treeData;
      }

      const filterLowerCase = props.filter.trim().toLowerCase();
      const matchesCurrentNode = treeData.name
        .toLowerCase()
        .includes(filterLowerCase);

      const filteredChildren = treeData.children
        ?.map((child) => filterTree(child))
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
    };

    const renderTree = async (
      sbomDependencies: SbomDependency[],
    ): Promise<void> => {
      const treeData: DependencyTreeData = normalizeData(sbomDependencies);
      rootNode.value = await createDependencyTree(
        treeContainer.value!,
        filterTree(treeData),
        !props.filter.trim(),
      );
    };

    const updateTree = async (): Promise<void> => {
      await rerenderDependencyTree(
        rootNode.value,
        filterTree(normalizeData(dependencies.value)),
      );
    };

    const rerenderOrUpdateTree = async (
      newVal: string,
      oldVal: string,
    ): Promise<void> => {
      if (dependencies.value.length > 0) {
        if (!newVal.trim() || newVal === oldVal) {
          await renderTree(dependencies.value);
        } else {
          await updateTree();
        }
      }
    };

    const debounceRerender = (newVal: string, oldVal: string): void => {
      if (oldVal !== newVal && treeContainer.value !== null) {
        clearTimeout(timeout!);
        timeout = window.setTimeout(async () => {
          await rerenderOrUpdateTree(newVal, oldVal);
        }, 1000);
      }
    };

    onMounted(() => {
      fetchSbomDependencies(props.sbomId);
    });

    watch(() => props.filter, debounceRerender, { immediate: true });

    return {
      treeContainer,
      error,
    };
  },
};
</script>

<style scoped>
.x-scroller {
  overflow-x: scroll;
}
</style>
