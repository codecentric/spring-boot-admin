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
  <div class="field is-grouped is-grouped-multiline" v-if="hasTags">
    <div class="control" v-for="(value, key) in tags" :key="key">
      <div class="tags has-addons">
        <span class="tag is-dark" v-text="key" />
        <span class="tag is-light" v-text="value" />
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    props: {
      tags: {
        type: Object,
        required: true
      }
    },
    computed: {
      hasTags() {
        return Object.keys(this.tags).length > 0;
      }
    }
  }
</script>
