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
  <div class="application-summary">
    <div class="pr-3 text-center">
      <sba-status :status="application.status" />
    </div>
    <div class="flex-1">
      <div class="font-semibold" v-text="application.name" />
      <small>
        <a
          v-if="application.instances.length === 1"
          :href="healthUrl"
          v-text="healthUrl"
        />
        <span v-else v-text="`${application.instances.length} instances`" />
      </small>
    </div>
    <p class="hidden md:block w-1/4" v-text="application.buildVersion" />
  </div>
</template>

<script>
import Application from '@/services/application';

export default {
  name: 'ApplicationsListItemSummary',
  props: {
    application: {
      type: Application,
      required: true,
    },
  },
  computed: {
    healthUrl() {
      if (this.application.instances.length === 1) {
        return (
          this.application.instances[0].registration.serviceUrl ||
          this.application.instances[0].registration.healthUrl
        );
      } else {
        return '';
      }
    },
  },
};
</script>

<style scoped>
.application-summary {
  display: contents;
}
</style>
