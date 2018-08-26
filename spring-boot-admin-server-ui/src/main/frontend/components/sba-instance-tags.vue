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
    <div class="control" v-for="tag in instance.tags" :key="tag.key">
      <div class="tags has-addons">
        <span class="tag" :class="keyColor" v-text="tag.key"/>
        <span class="tag" :class="valueColor" v-text="tag.value"/>
      </div>
    </div>
  </div>
</template>

<script>
  import Instance from '@/services/instance';
  
  export default {
    props: {
      instance: {
        type: Instance,
        default: null,
      },
      keyColor: {
        type: String,
        default: "is-dark"
      },
      valueColor: {
        type: String,
        default: "is-light"
      }
    },
    methods: {
      hasTags() {
        return this.instance && Array.isArray(this.instance.tags) && this.instance.tags.length > 0;
      }
    }
  }
</script>
