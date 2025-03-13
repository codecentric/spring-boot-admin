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
  <sba-button
    v-on-clickaway="abort"
    :class="{ 'is-success': confirm }"
    :title="title"
    @click="click"
  >
    <slot v-if="confirm" name="confirm">
      {{ $t('term.confirm') }}
    </slot>
    <slot v-else />
  </sba-button>
</template>

<script>
import { directive as onClickaway } from 'vue3-click-away';

export default {
  directives: { onClickaway },
  props: {
    title: {
      type: String,
      default: '',
    },
  },
  emits: ['click'],
  data() {
    return {
      confirm: false,
    };
  },
  methods: {
    abort() {
      this.confirm = false;
    },
    click(event) {
      if (this.confirm) {
        this.$emit('click', event);
      } else {
        event.stopPropagation();
      }
      this.confirm = !this.confirm;
    },
  },
};
</script>
