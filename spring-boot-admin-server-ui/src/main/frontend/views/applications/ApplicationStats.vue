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
  <div class="hidden md:flex mr-1 gap-1">
    <sba-tag
      :label="$t('applications.applications')"
      :value="applicationsCount"
    />
    <sba-tag :label="$t('applications.instances')" :value="instancesCount" />
  </div>
</template>

<script setup>
import { computed } from 'vue';

import { useApplicationStore } from '@/composables/useApplicationStore';

const { applications } = useApplicationStore();
const applicationsCount = computed({
  get() {
    return applications.value.length;
  },
});

const instancesCount = computed({
  get() {
    return applications.value.reduce(
      (current, next) => current + next.instances.length,
      0,
    );
  },
});
</script>
