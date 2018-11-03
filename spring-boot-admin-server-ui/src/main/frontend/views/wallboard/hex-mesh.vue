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
  <div class="hex-mesh" v-on-resize="onResize">
    <svg xmlns="http://www.w3.org/2000/svg" :width="meshWidth" :height="meshHeight">
      <defs>
        <clipPath id="hex-clip">
          <polygon :points="hexPath" />
        </clipPath>
      </defs>
      <template>
        <template v-for="row in rows">
          <g v-for="col in cols + (row % 2 ? 0 : -1)" :key="`${col}-${row}`"
             class="hex" :transform="translate(col, row)"
             :class="classForItem(item(col,row))"
             @click="click($event,col,row)"
          >
            <polygon :points="hexPath" />
            <foreignObject v-if="item(col,row)" x="0" y="0" :width="hexWidth" :height="hexHeight"
                           style="pointer-events: none"
            >
              <slot name="item" v-bind="item(col,row)" />
            </foreignObject>
          </g>
        </template>
      </template>
    </svg>
  </div>
</template>

<script>
  import onResize from '@/directives/on-resize';

  const tileCount = (cols, rows) => {
    const shorterRows = Math.floor(rows / 2);
    return rows * cols - shorterRows;
  };

  const calcSideLength = (width, height, cols, rows) => {
    const fitToWidth = width / cols / Math.sqrt(3);
    const fitToHeight = height * 2 / (3 * rows + 1);
    return Math.min(fitToWidth, fitToHeight);
  };

  const calcLayout = (minTileCount, width, height) => {
    let cols = 1, rows = 1;
    let sideLength = calcSideLength(width, height, cols, rows);

    while (minTileCount > tileCount(cols, rows)) {
      const sidelengthExtraCol = calcSideLength(width, height, cols + 1, rows);
      const sidelengthExtraRow = calcSideLength(width, height, cols, rows + 1);
      if (sidelengthExtraCol > sidelengthExtraRow) {
        sideLength = sidelengthExtraCol;
        cols++;
      } else {
        sideLength = sidelengthExtraRow;
        rows++
      }
    }
    return {
      cols,
      rows,
      sideLength
    }
  };

  export default {
    props: {
      items: {
        type: Array,
        default: () => []
      },
      classForItem: {
        type: Function,
        default: () => {
        }
      }
    },
    directives: {onResize},
    data: () => ({
      cols: 1,
      rows: 1,
      sideLength: 1
    }),
    methods: {
      translate(col, row) {
        const x = (col - 1) * this.hexWidth + (row % 2 ? 0 : this.hexWidth / 2);
        const y = (row - 1) * this.sideLength * 1.5;
        return `translate(${x},${y})`;
      },
      item(col, row) {
        const rowOffset = (row - 1) * this.cols - Math.max(Math.floor((row - 1) / 2), 0);
        const index = rowOffset + col - 1;
        return this.items[index];
      },
      point(i) {
        const innerSideLength = this.sideLength * 0.95;
        const marginTop = this.hexHeight / 2;
        const marginLeft = this.hexWidth / 2;
        return `${  marginLeft + (innerSideLength * Math.cos((1 + i * 2) * Math.PI / 6))},${marginTop + (innerSideLength * Math.sin((1 + i * 2) * Math.PI / 6))}`
      },
      click(event, col, row) {
        const item = this.item(col, row);
        if (item) {
          this.$emit('click', item, event);
        }
      },
      updateLayout() {
        if (this.$el) {
          const boundingClientRect = this.$el.getBoundingClientRect();
          const layout = calcLayout(this.itemCount, boundingClientRect.width, boundingClientRect.height);
          this.cols = layout.cols;
          this.rows = layout.rows;
          this.sideLength = layout.sideLength;
        }
      },
      onResize(entries) {
        for (let e of entries) {
          if (e.target === this.$el) {
            this.updateLayout();
          }
        }
      }
    },
    computed: {
      itemCount() {
        return this.items.length;
      },
      hexPath() {
        return `${this.point(0)} ${this.point(1)} ${this.point(2)} ${this.point(3)} ${this.point(4)} ${this.point(5)}`
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
      }
    },
    watch: {
      sideLength(newVal) {
        this.$el.style['font-size'] = `${newVal / 9.5}px`;
      },
      itemCount: {
        handler: 'updateLayout',
        immediate: true
      }
    }
  };

  export {calcLayout};
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .hex-mesh {
    background-color: $grey-dark;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: space-around;
    align-items: center;
  }

  .hex {
    fill-opacity: 0.05;
    stroke-width: 0.5;
    stroke-opacity: 0.8;

    & polygon {
      fill: none;
      stroke: $grey;
      transition: all $easing 250ms;
    }

    @each $name, $pair in $colors {
      &.is-#{$name} polygon {
        $color: nth($pair, 1);
        fill: $color;
        fill-opacity: 0.3;
        stroke: $color;
        stroke-opacity: 0.95;
        stroke-width: 1.5;
      }
    }

    &.is-selectable:hover {
      cursor: pointer;
      & polygon {
        fill-opacity: 0.85;
        stroke-opacity: 1;
      }
    }

    &__body {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
    }
  }
</style>
