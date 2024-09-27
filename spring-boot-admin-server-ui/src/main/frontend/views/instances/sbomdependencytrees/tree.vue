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
  <sba-instance-section :error="error" :loading="isLoading">
    <sba-panel :title="sbomId">
      <div ref="treeContainer" class="x-scroller"></div>
    </sba-panel>
  </sba-instance-section>
</template>

<script lang="ts">
import { debounce } from 'lodash';
import { computed, onMounted, ref, watch } from 'vue';

import SbaPanel from '@/components/sba-panel.vue';

import Instance from '@/services/instance';
import {
  D3DependencyTree,
  createDependencyTree,
  rerenderDependencyTree,
} from '@/views/instances/sbomdependencytrees/dependencyTree';
import {
  SbomDependency,
  filterTree,
  normalizeData,
} from '@/views/instances/sbomdependencytrees/sbomUtils';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section.vue';

export default {
  name: 'TreeGraph',
  components: { SbaInstanceSection, SbaPanel },
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
    const dependencies = ref<SbomDependency[]>([]);
    const rootNode = ref<D3DependencyTree | null>(null);
    const error = ref<string | null>(null);
    const isLoading = ref<boolean | null>(false);

    const normalizedData = computed(() => normalizeData(dependencies.value));
    const filteredData = computed(() =>
      filterTree(normalizedData.value, props.filter),
    );

    const fetchSbomDependencies = async (sbomId: string): Promise<void> => {
      error.value = null;
      isLoading.value = true;
      try {
        const res = await props.instance.fetchSbom(sbomId);
        dependencies.value = res.data.dependencies;
        await renderTree();
      } catch (err) {
        console.warn('Fetching sbom failed:', err);
        error.value = err;
      } finally {
        isLoading.value = false;
      }
    };

    const renderTree = async (): Promise<void> => {
      rootNode.value = await createDependencyTree(
        treeContainer.value!,
        filteredData.value,
      );
    };

    const updateTree = async (): Promise<void> => {
      isLoading.value = true;
      await rerenderDependencyTree(rootNode.value, filteredData.value);
      isLoading.value = false;
    };

    const rerenderOrUpdateTree = async (
      newVal: string,
      oldVal: string,
    ): Promise<void> => {
      if (dependencies.value.length > 0) {
        if (!newVal.trim() || newVal === oldVal) {
          await renderTree();
        } else {
          await updateTree();
        }
      }
    };

    const debouncedRerenderOrUpdateTree = debounce(rerenderOrUpdateTree, 1000);

    watch(
      () => props.filter,
      (newVal, oldVal) => {
        if (newVal !== oldVal && treeContainer.value !== null) {
          debouncedRerenderOrUpdateTree(newVal, oldVal);
        }
      },
      { immediate: true },
    );

    onMounted(() => {
      fetchSbomDependencies(props.sbomId);
    });

    return {
      treeContainer,
      error,
      isLoading,
    };
  },
};
</script>

<style scoped>
.x-scroller {
  overflow-x: scroll;
}
</style>
