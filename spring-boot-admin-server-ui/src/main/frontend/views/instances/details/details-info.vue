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
    <table class="table">
        <tr v-for="(value, key) in info" :key="key">
            <td class="info__key" v-text="key"></td>
            <td>
                <sba-formatted-obj :value="value"></sba-formatted-obj>
            </td>
        </tr>
    </table>
</template>

<script>

  export default {
    props: ['instance'],
    data: () => ({
      info: null,
    }),
    created() {
      this.fetchInfo();
    },
    watch: {
      instance(newVal, oldVal) {
        if (newVal !== oldVal) {
          this.fetchInfo();
        }
      }
    },
    methods: {
      async fetchInfo() {
        if (this.instance) {
          const res = await this.instance.fetchInfo();
          this.info = res.data;
        }
      }
    }
  }
</script>

<style lang="scss">
    .info__key {
        vertical-align: top;
    }
</style>