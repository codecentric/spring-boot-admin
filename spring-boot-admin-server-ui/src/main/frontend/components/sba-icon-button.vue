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
    <button type="button" class="icon-button" :disabled="disabled">
        <slot></slot>
    </button>
</template>

<script>
  export default {
    props: {
      disabled: Boolean,
    },
  }
</script>

<style lang="scss">

    .icon-button {
        background: none;
        border: none;
        padding: 0;

        &:not([disabled]) {
            cursor: pointer;
            &:hover {
            }
        }

        &:disabled {
            pointer-events: none;
        }

        &:active {
            outline: none;
        }
    }
</style>