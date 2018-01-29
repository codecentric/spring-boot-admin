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
    <sba-panel title="Health">
        <div slot="text">
            <div v-if="error" class="message is-danger">
                <div class="message-body">
                    <strong>
                        <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"></font-awesome-icon>
                        Fetching health failed.
                    </strong>
                </div>
            </div>
            <div class="content" :class="{ 'is-loading' : !hasLoaded }">
                <health-default v-if="health" name="Instance" :health="health"></health-default>
            </div>
        </div>
    </sba-panel>
</template>

<script>
  import healthDefault from './health/health-default';

  export default {
    components: {healthDefault},
    props: ['instance'],
    data: () => ({
      hasLoaded: false,
      error: null,
      health: null,
    }),
    created() {
      this.fetchHealth();
    },
    watch: {
      instance() {
        this.fetchHealth();
      }
    },
    methods: {
      async fetchHealth() {
        if (this.instance) {
          this.error = null;
          try {
            const res = await this.instance.fetchHealth();
            this.health = res.data;
          } catch (error) {
            console.warn('Fetching health failed:', error);
            this.error = error;
          }
          this.hasLoaded = true;
        }
      }
    }
  }
</script>