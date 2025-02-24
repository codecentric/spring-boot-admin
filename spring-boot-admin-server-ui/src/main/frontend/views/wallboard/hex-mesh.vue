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
  <div ref="root" v-on-resize="onResize" class="hex-mesh">
    <svg
      :height="meshHeight"
      :width="meshWidth"
      xmlns="http://www.w3.org/2000/svg"
    >
      <defs>
        <clipPath id="hex-clip">
          <polygon :points="hexPath" />
        </clipPath>
      </defs>
      <template v-for="row in rows">
        <g
          v-for="col in cols + (row % 2 ? 0 : -1)"
          :key="`${col}-${row}`"
          :class="classForItem(item(col, row))"
          :transform="translate(col, row)"
          class="hex"
          @click="click($event, col, row)"
        >
          <polygon :points="hexPath" />
          <foreignObject
            v-if="item(col, row)"
            :height="hexHeight"
            :width="hexWidth"
            style="pointer-events: none"
            x="0"
            y="0"
          >
            <slot :item="item(col, row)" name="item" />
          </foreignObject>
        </g>
      </template>
    </svg>
  </div>
</template>

<script>
import { ref } from 'vue';

import onResize from '@/directives/on-resize';
import { calcLayout } from '@/views/wallboard/utils';

export default {
  directives: { onResize },
  props: {
    items: {
      type: Array,
      default: () => [],
    },
    classForItem: {
      type: Function,
      default: () => void 0,
    },
  },
  emits: ['click'],
  setup() {
    const root = ref(null);

    return {
      root,
    };
  },
  data: () => ({
    cols: 1,
    rows: 1,
    sideLength: 1,
  }),
  computed: {
    itemCount() {
      return this.items.length;
    },
    hexPath() {
      return `${this.point(0)} ${this.point(1)} ${this.point(2)} ${this.point(
        3,
      )} ${this.point(4)} ${this.point(5)}`;
    },
    hexHeight() {
      return this.sideLength * 2;
    },
    hexWidth() {
      return this.sideLength * Math.sqrt(3);
    },
    meshWidth() {
      return this.hexWidth * this.cols;
    },
    meshHeight() {
      return this.sideLength * (2 + (this.rows - 1) * 1.5);
    },
  },
  watch: {
    sideLength(newVal) {
      this.root.style['font-size'] = `${newVal / 9.5}px`;
    },
    itemCount: {
      handler: 'updateLayout',
      immediate: true,
    },
  },
  methods: {
    translate(col, row) {
      const x = (col - 1) * this.hexWidth + (row % 2 ? 0 : this.hexWidth / 2);
      const y = (row - 1) * this.sideLength * 1.5;
      return `translate(${x},${y})`;
    },
    item(col, row) {
      const rowOffset =
        (row - 1) * this.cols - Math.max(Math.floor((row - 1) / 2), 0);
      const index = rowOffset + col - 1;
      return this.items[index];
    },
    point(i) {
      const innerSideLength = this.sideLength * 0.95;
      const marginTop = this.hexHeight / 2;
      const marginLeft = this.hexWidth / 2;
      return `${
        marginLeft + innerSideLength * Math.cos(((1 + i * 2) * Math.PI) / 6)
      },${marginTop + innerSideLength * Math.sin(((1 + i * 2) * Math.PI) / 6)}`;
    },
    click(event, col, row) {
      const item = this.item(col, row);
      if (item) {
        this.$emit('click', item, event);
      }
    },
    updateLayout() {
      if (this.root) {
        const boundingClientRect = this.root.getBoundingClientRect();
        const layout = calcLayout(
          this.itemCount,
          boundingClientRect.width,
          boundingClientRect.height,
        );
        this.cols = layout.cols;
        this.rows = layout.rows;
        this.sideLength = layout.sideLength;
      }
    },
    onResize(entries) {
      for (let e of entries) {
        if (e.target === this.root) {
          this.updateLayout();
        }
      }
    },
  },
};
</script>

<style>
.hex-mesh {
  background-color: #4a4a4a;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.hex {
  cursor: pointer;
  fill-opacity: 0.05;
  stroke-width: 0.5;
  stroke-opacity: 0.8;
}

.hex polygon {
  fill: transparent;
  transition: all ease-out 250ms;
}

.hex:hover polygon {
  fill-opacity: 0.25;
  stroke-opacity: 1;
  stroke-width: 2;
}
</style>
