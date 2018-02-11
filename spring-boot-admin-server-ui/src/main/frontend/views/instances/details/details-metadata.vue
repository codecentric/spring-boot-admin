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
    <sba-panel title="Metadata">
        <div class="content metadata" v-if="metadata">
            <table class="table" v-if="!isEmptyMetadata">
                <tr v-for="(value, key) in metadata" :key="key">
                    <td class="metadata__key" v-text="key"></td>
                    <td>
                        <sba-formatted-obj :value="value"></sba-formatted-obj>
                    </td>
                </tr>
            </table>
            <p v-else class="is-muted">No metadata provided.</p>
        </div>
    </sba-panel>
</template>

<script>
  export default {
    props: ['instance'],
    computed: {
      metadata() {
        return this.instance.registration.metadata;
      },
      isEmptyMetadata() {
        return Object.keys(this.metadata).length <= 0;
      }
    },
  }
</script>

<style lang="scss">
    .metadata {
        overflow: auto;

        &__key {
            vertical-align: top;
        }
    }
</style>
