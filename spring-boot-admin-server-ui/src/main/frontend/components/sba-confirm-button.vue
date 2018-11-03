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
  <button @click="click" class="confirm-button" :class="{ 'is-warning' : confirm }" v-on-clickaway="abort">
    <slot name="confirm" v-if="confirm">Confirm</slot>
    <slot v-else />
  </button>
</template>

<script>
  import {directive as onClickaway} from 'vue-clickaway';

  export default {
    directives: {onClickaway},
    data: () => ({
      confirm: false
    }),
    methods: {
      abort() {
        this.confirm = false;
      },
      click(event) {
        if (this.confirm) {
          this.$el.style.width = null;
          this.$emit('click', event);
        } else {
          const width = this.$el.getBoundingClientRect().width;
          this.$el.style.width = `${width}px`;
        }
        this.confirm = !this.confirm;
      }
    }
  }
</script>


<style lang="scss">
  @import "~@/assets/css/utilities";

  .confirm-button {
    transition: all $easing 150ms;
  }
</style>
