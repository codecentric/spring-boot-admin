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
  <sba-panel :title="$t('instances.details.info.title')" :loading="loading">
    <sba-alert
      v-if="error"
      :error="error"
      class="border-l-4"
      :title="$t('term.fetch_failed')"
    />
    <div v-else class="content info">
      <table v-if="!isEmptyInfo" class="table">
        <tr v-for="(value, key) in info" :key="key">
          <td class="info__key" v-text="key" />
          <td>
            <sba-formatted-obj :value="value" />
          </td>
        </tr>
      </table>
      <p
        v-else
        class="is-muted"
        v-text="$t('instances.details.info.no_info_provided')"
      />
    </div>
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
  data: () => ({
    error: null,
    loading: false,
    liveInfo: null,
  }),
  computed: {
    info() {
      return this.liveInfo || this.instance.info;
    },
    isEmptyInfo() {
      return Object.keys(this.info).length <= 0;
    },
  },
  created() {
    this.fetchInfo();
  },
  methods: {
    async fetchInfo() {
      if (this.instance.hasEndpoint('info')) {
        this.loading = true;
        this.error = null;

        try {
          const res = await this.instance.fetchInfo();
          this.liveInfo = res.data;
        } catch (error) {
          this.error = error;
          console.warn('Fetching info failed:', error);
        } finally {
          this.loading = false;
        }
      }
    },
  },
};
</script>

<style lang="css">
.info {
  overflow: auto;
}

.info__key {
  vertical-align: top;
}
</style>
