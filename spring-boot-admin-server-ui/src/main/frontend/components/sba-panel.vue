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
  <div class="card panel">
    <header
      v-if="title || $slots['header']"
      class="card-header"
      :class="{'panel__header--sticky': headerSticksBelow}"
      v-sticks-below="headerSticksBelow"
    >
      <p class="card-header-title">
        <span v-text="title" />
        <slot name="header" />
      </p>
      <div class="panel__close">
        <sba-icon-button v-if="closeable" :icon="['far', 'times-circle']" @click.stop="close" />
      </div>
    </header>
    <div v-if="$slots['default']" class="card-content">
      <slot />
    </div>
  </div>
</template>

<script>
  import sticksBelow from '@/directives/sticks-below';
  import SbaIconButton from './sba-icon-button';

  export default {
    components: {SbaIconButton},
    directives: {sticksBelow},
    props: {
      title: {
        type: String,
        required: true
      },
      closeable: {
        type: Boolean,
        default: false
      },
      headerSticksBelow: {
        type: Array,
        default: undefined
      }
    },
    methods: {
      close(event) {
        this.$emit('close', event);
      }
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .panel {
    margin-bottom: 1.5rem;

    &__close {
      margin-right: 0.75em;
      color: $grey-light;
      display: flex;
      align-items: center;
      justify-self: flex-end;
    }

    &__header--sticky {
      position: sticky;
      background-color: $white;
    }
  }
</style>
