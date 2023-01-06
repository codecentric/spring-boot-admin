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
  <sba-panel :title="$t('instances.details.metadata.title')" :seamless="true">
    <sba-key-value-table v-if="!isEmptyMetadata" :map="metadata" />
  </sba-panel>
</template>

<script>
import Instance from '@/services/instance';

export default {
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  computed: {
    metadata() {
      return this.instance.registration.metadata;
    },
    isEmptyMetadata() {
      return Object.keys(this.instance.registration.metadata).length <= 0;
    },
  },
};
</script>
