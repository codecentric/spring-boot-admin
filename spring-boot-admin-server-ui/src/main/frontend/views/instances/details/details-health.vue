<!--
  - Copyright 2014-2019 the original author or authors.
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
  <sba-panel :title="$t('instances.details.health.title')" :loading="loading">
    <template #actions>
      <router-link
        :to="{ name: 'journal', query: { instanceId: instance.id } }"
        class="text-sm inline-flex items-center leading-sm border border-gray-400 bg-white text-gray-700 rounded overflow-hidden px-3 py-1 hover:bg-gray-200 ml-1"
      >
        <font-awesome-icon icon="history" />
      </router-link>
    </template>

    <template #default>
      <sba-alert
        :error="error"
        class="border-l-4"
        :title="$t('term.fetch_failed')"
      />
      <div class="-mx-4 -my-3">
        <health-details :health="health" name="Instance" />
      </div>
    </template>
  </sba-panel>
</template>

<script>
import Instance from '@/services/instance';
import healthDetails from '@/views/instances/details/health-details';

export default {
  components: { healthDetails },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    error: null,
    loading: false,
    liveHealth: null,
  }),
  computed: {
    health() {
      return this.liveHealth || this.instance.statusInfo;
    },
  },
  created() {
    this.fetchHealth();
  },
  methods: {
    async fetchHealth() {
      this.error = null;
      this.loading = true;
      try {
        const res = await this.instance.fetchHealth();
        this.liveHealth = res.data;
      } catch (error) {
        console.warn('Fetching live health failed:', error);
        this.error = error;
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>
