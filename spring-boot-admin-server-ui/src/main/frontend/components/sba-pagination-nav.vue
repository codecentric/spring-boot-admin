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
  <div class="field has-addons has-addons-centered">
    <p class="control">
      <button class="button" @click="$emit('change', current-1)" :disabled="current <= 1">
        <span class="icon is-small">
          <font-awesome-icon :icon="['fas','angle-double-left']" />
        </span>
      </button>
    </p>
    <p class="control" v-for="page in pageCount" :key="'page' + page">
      <button class="button" :class="{'is-active': page === current}" @click="$emit('change', page)">
        <span>{{ page }}</span>
      </button>
    </p>
    <p class="control">
      <button class="button" @click="$emit('change', current+1)" :disabled="current >= pageCount">
        <span class="icon is-small">
          <font-awesome-icon :icon="['fas','angle-double-right']" />
        </span>
      </button>
    </p>
  </div>
</template>

<script>
export default {
  name: 'SbaPaginationNav',
  model: {
    prop: 'current',
    event: 'change'
  },
  props: {
    current: {type: Number, default: 1},
    pageCount: {type: Number, required: true}
  }
}
</script>

<style scoped>

</style>
