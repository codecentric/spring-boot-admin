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
      <div class="column column--name" v-text="$t('instances.startup.column.name')" />
      <div class="column column--duration" v-text="$t('instances.startup.column.duration')" />
      <div class="column column--details" v-text="$t('instances.startup.column.details')" />
    </div>
    <ul>
      <tree-item
        v-for="(eventNode, index) in tree.getRoots()"
        :key="index"
        :item="eventNode"
        :tree="tree"
      />
    </ul>
  </div>
</template>

<script>
import TreeItem from '@/views/instances/startup/tree-item';
import {StartupActuatorEventTree} from '@/services/startup-actuator';

export default {
  components: {TreeItem},
  props: {
    tree: {
      type: StartupActuatorEventTree,
      required: true
    }
  },
}
</script>

<style lang="scss">
.text-right {
  text-align: right;
}

.text-center {
  text-align: center;
}

.enforce-word-wrap {
  word-break: break-all;
}

.tree {
  .row {
    display: grid;
    grid-template-columns: 1fr 140px 1fr;
    grid-template-rows: 1fr;
    grid-column-gap: 0;
    grid-row-gap: 0;
    border-bottom: 1px solid #dbdbdb;

    &--head {
      background-color: #fff;
      position: sticky;
      top: 54px;
      grid-template-rows: 1fr;
      color: #363636;
      border: none;
      border-bottom: 2px solid #dbdbdb;
      vertical-align: top;
      font-weight: 700;
      padding-top: 0.5em;
      padding-bottom: 0.5em;
    }

    .column {
      padding: 0.5em 0.75em;
      overflow: hidden;
      text-overflow: ellipsis;

      &--name { grid-area: 1 / 1 / 2 / 2; }
      &--duration { grid-area: 1 / 2 / 2 / 3; }
      &--details { grid-area: 1 / 3 / 2 / 4; }
    }
  }
}

.tree-item {
  margin: 0;
  transition-property: display;
  transition-duration: 125ms;

  .icon {
    $iconSize: 6px;
    width: $iconSize * 1.2;
    height: $iconSize;

    margin-right: 10px;

    border-top: $iconSize solid transparent;
    border-left: $iconSize*1.2 solid #555;
    border-bottom: $iconSize solid transparent;

    &--open {
      transform: rotate(90deg);
    }

    &.empty {
      border: none;
    }
  }

  $colors: #FFFFFF, #4CB4D4, #d4577b, #42d3a5, #D4802C, #37D449;
  @for $i from 1 through 5 {
    &[tree-item-depth='#{$i}'] {
      background-color: rgba(#42D3A5, 0.2);

      .column--name {
        padding-left: 22px * $i;
      }
    }
  }
}
</style>
