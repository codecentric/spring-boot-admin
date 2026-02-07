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
          <path :d="hexPath" />
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
          <path :d="hexPath" />
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
      const points = [
        this.point(0),
        this.point(1),
        this.point(2),
        this.point(3),
        this.point(4),
        this.point(5),
      ];

      // Radius for the rounded corners
      const cornerRadius = this.sideLength * 0.05;

      // Parse points into coordinate pairs
      const coords = points.map((p) => {
        const [x, y] = p.split(',').map(Number);
        return { x, y };
      });

      // Helper function to calculate distance between two points
      const distance = (p1, p2) =>
        Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));

      // Helper function to move along a line from p1 towards p2 by a given distance
      const moveAlong = (p1, p2, dist) => {
        const d = distance(p1, p2);
        const ratio = dist / d;
        return {
          x: p1.x + (p2.x - p1.x) * ratio,
          y: p1.y + (p2.y - p1.y) * ratio,
        };
      };

      // Build the path
      let path = '';

      for (let i = 0; i < coords.length; i++) {
        const current = coords[i];
        const prev = coords[(i - 1 + coords.length) % coords.length];
        const next = coords[(i + 1) % coords.length];

        // Point after current corner (moving from current towards next)
        const nextEdgeLength = distance(current, next);
        const afterCorner = moveAlong(
          current,
          next,
          Math.min(cornerRadius, nextEdgeLength / 2),
        );

        // Point before current corner (moving from current towards prev)
        const prevEdgeLength = distance(current, prev);
        const beforeCorner = moveAlong(
          current,
          prev,
          Math.min(cornerRadius, prevEdgeLength / 2),
        );

        if (i === 0) {
          // Start at the point after the first corner
          path += `M ${afterCorner.x},${afterCorner.y} `;
        } else {
          // Draw line to the point before this corner
          path += `L ${beforeCorner.x},${beforeCorner.y} `;
          // Draw quadratic bezier curve around this corner using the corner as control point
          path += `Q ${current.x},${current.y} ${afterCorner.x},${afterCorner.y} `;
        }
      }

      // Close the path (draws line back to start and bezier around first corner)
      const firstCorner = coords[0];
      const lastCorner = coords[coords.length - 1];
      const firstEdgeLength = distance(firstCorner, lastCorner);
      const beforeFirstCorner = moveAlong(
        firstCorner,
        lastCorner,
        Math.min(cornerRadius, firstEdgeLength / 2),
      );
      path += `L ${beforeFirstCorner.x},${beforeFirstCorner.y} `;

      const afterFirstCorner = moveAlong(
        firstCorner,
        coords[1],
        Math.min(cornerRadius, distance(firstCorner, coords[1]) / 2),
      );
      path += `Q ${firstCorner.x},${firstCorner.y} ${afterFirstCorner.x},${afterFirstCorner.y} `;

      path += 'Z';
      return path;
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
      const x =
        marginLeft + innerSideLength * Math.cos(((1 + i * 2) * Math.PI) / 6);
      const y =
        marginTop + innerSideLength * Math.sin(((1 + i * 2) * Math.PI) / 6);
      return `${x},${y}`;
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

.hex:hover path {
  fill-opacity: 0.25;
  stroke-opacity: 1;
  stroke-width: 2;
}
</style>
