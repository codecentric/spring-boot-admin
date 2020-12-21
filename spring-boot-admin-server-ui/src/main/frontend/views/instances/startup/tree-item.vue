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
  <li class="tree-item" :tree-item-depth="item.startupStep.depth" :class="{'is-open': isOpen}">
    <div class="row">
      <div class="column column--name">
        <a class="icon" :class="{'empty': !hasChildren, 'icon--open': isOpen}" @click="toggle" />
        <span v-text="item.startupStep.name" />&nbsp;<small>(#<span v-text="item.startupStep.id" />)</small>
      </div>
      <div class="column column--duration monospaced text-right" v-text="item.duration.toFixed(4)" :title="item.duration + 'ms'" />
      <div class="column column--details">
        <span v-for="(tag, index) in item.startupStep.tags" :key="index">
          <strong>{{ tag.key }}: </strong>
          <span v-text="tag.value" class="enforce-word-wrap" />
          <br>
        </span>
      </div>
    </div>
    <ul v-show="isOpen">
      <tree-item
        v-for="(child, index) in item.startupStep.children"
        :key="index"
        :item="child"
        :expand="expand"
        @toggle="onToggle"
      />
    </ul>
  </li>
</template>

<script>

export default {
  name: 'TreeItem',
  props: {
    item: {
      type: Object,
      required: true
    },
    expand: Set
  },
  data: () => ({
    isOpen: false,
  }),
  created() {
      this.isOpen = this.expand.has(this.item.startupStep.id);
  },
  watch: {
    expand: function (newVal) {
      this.isOpen = newVal.has(this.item.startupStep.id);
    }
  },
  computed: {
    hasChildren: function () {
      return this.item.startupStep.children && this.item.startupStep.children.length;
    }
  },
  methods: {
    onToggle: function ($event) {
      this.$emit('toggle', $event);
    },
    toggle: function () {
      if (this.hasChildren) {
        this.isOpen = !this.isOpen;
        this.$emit('toggle', {
          target: this.item,
          isOpen: this.isOpen
        });
      }
    },
  }
}
</script>
