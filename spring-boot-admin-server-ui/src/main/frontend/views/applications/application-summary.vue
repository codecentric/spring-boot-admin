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
    <sba-status :status="application.status"
                :date="application.statusTimestamp"
                class="application-summary__status"
    />
    <p class="application-summary__name">
      <span v-text="application.name" /><br>
      <span class="is-muted">
        <a v-if="application.instances.length === 1"
           v-text="healthUrl"
           :href="healthUrl"
        />
        <span v-else v-text="`${application.instances.length} instances`" />
      </span>
    </p>
    <p class="application-summary__version" v-text="application.buildVersion" />
  </div>
</template>
<script>
  import Application from '../../services/application';

  export default {
    props: {
      application: {
        type: Application,
        required: true
      }
    },
    computed: {
      healthUrl() {
        if (this.application.instances.length === 1) {
          return this.application.instances[0].registration.serviceUrl || this.application.instances[0].registration.healthUrl;
        } else {
          return ''
        }
      }
    }
  }
</script>
<style lang="scss">
  @import "~@/assets/css/utilities";

  .application-summary {
    display: contents;

    &__status {
      width: $gap;
    }

    &__name,
    &__version {
      flex-grow: 1;
      flex-basis: 50%;
    }

    &__name.title {
      margin: 0.75rem 0;
    }
  }
</style>
