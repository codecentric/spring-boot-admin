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
  <span class="tag thread-tag" :class="`thread-tag--${threadState.toLowerCase()}`" :title="threadState" />
</template>

<script>
  export default {
    props: {
      threadState: {
        type: String,
        required: true
      }
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .thread-tag {
    width: 2em;

    &--runnable {
      color: $success-invert !important;
      background-color: $success !important;

      &:before {
        content: 'R';
      }
    }

    &--timed_waiting,
    &--waiting {
      color: $warning-invert !important;
      background-color: $warning !important;

      &:before {
        content: 'W';
      }
    }

    &--blocked {
      color: $danger-invert !important;
      background-color: $danger !important;

      &:before {
        content: 'B';
      }
    }

    &--terminated {
      color: $light-invert !important;
      background-color: $light !important;

      &:before {
        content: 'T';
      }
    }

    &--new {
      color: $light-invert !important;
      background-color: $light !important;

      &:before {
        content: 'N';
      }
    }
  }
</style>
