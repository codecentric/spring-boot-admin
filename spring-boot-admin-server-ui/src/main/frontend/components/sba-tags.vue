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
  <div :class="classNames('inline-flex gap-1', { 'flex-wrap': wrap })">
    <template v-for="(value, key) in tags" :key="key">
      <sba-tag :label="key" :small="small" :value="value" />
    </template>
  </div>
</template>

<script>
import classNames from 'classnames';

export default {
  props: {
    tags: {
      type: Object,
      required: true,
    },
    small: {
      type: Boolean,
      default: false,
    },
    wrap: {
      type: Boolean,
      default: true,
    },
  },
  methods: {
    classNames: classNames,
  },
};
</script>
