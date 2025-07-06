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
  <pre class="formatted" v-html="formatted" />
</template>

<script lang="ts">
import { defineComponent } from 'vue';

import { createAutolink } from '@/utils/autolink';
import { objToYaml } from '@/utils/objToYaml';

const autolink = createAutolink({
  truncate: {
    length: 50,
    location: 'smart',
  },
});

export default defineComponent({
  props: {
    value: {
      type: null,
      required: true,
    },
  },
  computed: {
    formatted() {
      return autolink(objToYaml(this.value));
    },
  },
});
</script>

<style>
.formatted {
  white-space: break-spaces;
  word-break: break-word;
}
</style>
