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
    <table class="table is-fullwidth">
        <tr>
            <td>
                <sba-status v-if="health.status" :status="health.status"></sba-status>
                <span v-text="name"></span>
            </td>
        </tr>
        <tr v-if="details && details.length > 0">
            <td>
                <table class="table ">
                    <tr v-for="detail in details" :key="detail.name">
                        <td v-text="detail.name"></td>
                        <td v-text="detail.value"></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr v-for="child in childHealth" :key="child.name">
            <td>
                <health-diskspace v-if="child.name === 'diskSpace'"
                                  :name="child.name" :health="child.value"></health-diskspace>
                <health-default v-else :name="child.name" :health="child.value"></health-default>
            </td>
        </tr>
    </table>
</template>

<script>
  import _ from 'lodash';
  import healthDiskspace from './health-diskspace';

  const isChildHealth = (value) => {
    return value !== null && typeof value === 'object';
  };

  export default {
    name: 'health-default',
    components: {healthDiskspace},

    props: {
      name: String,
      health: Object
    },

    computed: {
      details() {
        if (this.health.details) {
          return _.entries(this.health.details)
            .filter(([, value]) => !isChildHealth(value))
            .map(([name, value]) => ({name, value}));
        }
        return [];
      },
      childHealth() {
        if (this.health.details) {
          return _.entries(this.health.details)
            .filter(([, value]) => isChildHealth(value))
            .map(([name, value]) => ({name, value}));
        }
        return [];
      }
    },
  }
</script>

<style lang="scss">
</style>