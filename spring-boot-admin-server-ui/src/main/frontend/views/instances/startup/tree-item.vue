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
  <li
    class="tree-item"
    :tree-item-depth="item.startupStep.depth"
    :class="{ 'is-open': isOpen }"
  >
    <div class="row">
      <div class="column column--name">
        <a
          class="icon"
          :class="{ empty: !hasChildren, 'icon--open': isOpen }"
          @click="toggle"
        >
          <svg
            v-if="hasChildren"
            aria-hidden="true"
            focusable="false"
            data-prefix="fas"
            role="img"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 448 512"
            class="w-3 h-3 -rotate-90"
          >
            <path
              fill="currentColor"
              d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z"
            />
          </svg>
        </a>
        <span v-text="item.startupStep.name" />&nbsp;<small
          >(#<span v-text="item.startupStep.id" />)</small
        >
      </div>
      <div
        class="column column--duration"
        :title="item.duration + 'ms'"
        v-text="item.duration.toFixed(4)"
      />
      <div class="column column--details">
        <span v-for="(tag, index) in item.startupStep.tags" :key="index">
          <strong>{{ tag.key }}: </strong>
          <span class="enforce-word-wrap" v-text="tag.value" />
          <br />
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
      required: true,
    },
    expand: Set,
  },
  emits: ['toggle'],
  data: () => ({
    isOpen: false,
  }),
  computed: {
    hasChildren: function () {
      return (
        this.item.startupStep.children && this.item.startupStep.children.length
      );
    },
  },
  watch: {
    expand: function (newVal) {
      this.isOpen = newVal.has(this.item.startupStep.id);
    },
  },
  created() {
    this.isOpen = this.expand.has(this.item.startupStep.id);
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
          isOpen: this.isOpen,
        });
      }
    },
  },
};
</script>
