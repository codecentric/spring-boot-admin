<!--
  - Copyright 2014-2019 the original author or authors.
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
  <button class="button icon-button" v-on="$listeners">
    <font-awesome-icon :icon="icon" :size="size" :class="iconClass" />
  </button>
</template>

<script>
  export default {
    props: {
      icon: {
        type: [String, Array],
        required: true,
      },
      size: {
        type: String,
        default: null
      },
      iconClass: {
        type: String,
        default: null
      }
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .icon-button {
    background: none;
    border: none;
    padding: 0;
    font-size: 1em;
    color: inherit;

    &:not([disabled]) {
      cursor: pointer;

      &:hover {
        color: $black;
      }

      & svg {
        fill: currentcolor;
      }
    }

    &:disabled {
      opacity: 0.5;
      pointer-events: none;
    }

    &:active {
      outline: none;
    }
  }
</style>
