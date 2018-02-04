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
    <sba-panel title="Info" v-if="hasLoaded">
        <div slot="text">
            <div v-if="error" class="message is-danger">
                <div class="message-body">
                    <strong>
                        <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"></font-awesome-icon>
                        Fetching info failed.
                    </strong>
                </div>
            </div>
            <div class="content info" v-if="info">
                <table class="table" v-if="!isEmptyInfo">
                    <tr v-for="(value, key) in info" :key="key">
                        <td class="info__key" v-text="key"></td>
                        <td>
                            <sba-formatted-obj :value="value"></sba-formatted-obj>
                        </td>
                    </tr>
                </table>
                <p v-else class="is-muted">No info provided.</p>
            </div>
        </div>
    </sba-panel>
</template>

<script>
  export default {
    props: ['instance'],
    data: () => ({
      hasLoaded: false,
      error: null,
      info: null
    }),
    computed: {
      isEmptyInfo() {
        return Object.keys(this.info).length <= 0;
      }
    },
    created() {
      this.fetchInfo();
    },
    watch: {
      instance() {
        this.fetchInfo();
      }
    },
    methods: {
      async fetchInfo() {
        if (this.instance && this.instance.hasEndpoint('info')) {
          try {
            this.error = null;
            const res = await this.instance.fetchInfo();
            this.info = res.data;
          } catch (error) {
            this.error = error;
            console.warn('Fetching info failed:', error);
          }
          this.hasLoaded = true;
        }
      }
    }
  }
</script>

<style lang="scss">
    .info {
        overflow: auto;

        &__key {
            vertical-align: top;
        }
    }

</style>