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
        <div class="content" slot="text">
            <health-default name="Instance" :health="health"></health-default>
        </div>
    </sba-panel>
</template>

<script>
  import healthDefault from './health/health-default.vue';

  export default {
    components: {
      healthDefault,
    },
    props: ['instance'],
    data: () => ({
      health: {status: 'UNKNOWN', details: []},
    }),
    created() {
      this.fetchHealth();
    },
    watch: {
      instance(newVal, oldVal) {
        if (newVal !== oldVal) {
          this.fetchHealth();
        }
      }
    },
    methods: {
      async fetchHealth() {
        if (this.instance) {
          const res = await this.instance.fetchHealth();
          this.health = res.data;
        }
      }
    }
  }
</script>

<style lang="scss">
</style>