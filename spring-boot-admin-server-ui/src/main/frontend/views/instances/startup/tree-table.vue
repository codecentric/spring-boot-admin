<!--
  - Copyright 2014-2018 the original author or authors.
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
  <div class="tree">
    <div class="row row--head">
      <div class="inline-flex items-center">
        <span v-text="$t('instances.startup.column.name')" />
      </div>
      <div
        class="text-right"
        v-text="$t('instances.startup.column.duration')"
      />
      <div class="break-all" v-text="$t('instances.startup.column.details')" />
    </div>
    <ul v-if="tree">
      <tree-item
        v-for="(eventNode, index) in tree.getRoots()"
        :key="index"
        :item="eventNode"
        :expand="expandedNodes"
        @toggle="onToggle"
      />
    </ul>
  </div>
</template>

<script>
import { StartupActuatorEventTree } from '@/services/startup-activator-tree';
import TreeItem from '@/views/instances/startup/tree-item';

export default {
  components: { TreeItem },
  props: {
    tree: {
      type: StartupActuatorEventTree,
      required: true,
    },
    expand: {
      type: Set,
      required: false,
      default: null,
    },
  },
  emits: ['change'],
  data: () => ({
    expandedNodes: new Set(),
    isExpanded: false,
  }),
  computed: {
    treeSize() {
      return new Set(this.tree.getEvents()).size;
    },
  },
  watch: {
    expand(expandedNodes) {
      this.expandedNodes = expandedNodes;
    },
    expandedNodes() {
      this.$emit('change', {
        expandedNodes: this.expandedNodes,
      });
    },
  },
  created() {
    if (this.expand) {
      this.expandedNodes = this.expand;
    }
  },
  methods: {
    onToggle($event) {
      if ($event.isOpen === true) {
        this.expandedNodes.add($event.target.startupStep.id);
        this.expandedNodes = new Set(this.expandedNodes);
      } else {
        this.expandedNodes.delete($event.target.startupStep.id);
        this.expandedNodes = new Set(this.expandedNodes);
      }

      this.isExpanded = this.expandedNodes.size === this.treeSize;
    },
  },
};
</script>

<style lang="css">
.text-right {
  @apply justify-end;
}
.text-center {
  @apply justify-center;
}
.enforce-word-wrap {
  @apply break-all;
}
.tree .checkbox-expand-all {
  margin-right: 5px;
}
.tree .row {
  display: grid;
  grid-template-columns: 1fr 140px 1fr;
  grid-template-rows: 1fr;
  grid-column-gap: 0;
  grid-row-gap: 0;
  border-bottom: 1px solid #dbdbdb;
}
.tree .row--head {
  @apply bg-white;
  padding: 0.5em 0.75em;
  grid-template-rows: 1fr;
  color: #363636;
  border: none;
  border-bottom: 2px solid #dbdbdb;
  vertical-align: top;
  font-weight: 700;
}
.tree .row .column {
  @apply truncate inline-flex items-center w-full;
  padding: 0.5em 0.75em;
}
.tree .row .column--details {
  grid-area: 0.0416666667;
  @apply break-all block;
}
.tree .row .column--duration {
  @apply text-sm font-mono text-right;
}
.tree-item {
  margin: 0;
  transition-property: transform;
  transition-duration: 125ms;
}
.tree-item .icon {
  @apply w-4 h-4 flex items-center cursor-pointer;
  margin-right: 10px;
}
.tree-item .icon--open {
  transform: rotate(90deg);
}
.tree-item .icon.empty {
}
.tree-item[tree-item-depth='1'] {
  background-color: rgba(66, 211, 165, 0.2);
}
.tree-item[tree-item-depth='1'] .column--name {
  padding-left: 22px;
}
.tree-item[tree-item-depth='2'] {
  background-color: rgba(66, 211, 165, 0.2);
}
.tree-item[tree-item-depth='2'] .column--name {
  padding-left: 44px;
}
.tree-item[tree-item-depth='3'] {
  background-color: rgba(66, 211, 165, 0.2);
}
.tree-item[tree-item-depth='3'] .column--name {
  padding-left: 66px;
}
.tree-item[tree-item-depth='4'] {
  background-color: rgba(66, 211, 165, 0.2);
}
.tree-item[tree-item-depth='4'] .column--name {
  padding-left: 88px;
}
.tree-item[tree-item-depth='5'] {
  background-color: rgba(66, 211, 165, 0.2);
}
.tree-item[tree-item-depth='5'] .column--name {
  padding-left: 110px;
}
</style>
