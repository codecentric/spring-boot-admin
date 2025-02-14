<!--
  - Copyright 2014-2020 the original author or authors.
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
  <dl
    class="px-4 py-3 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
    :class="{ 'bg-white': index % 2 === 0, 'bg-gray-50': index % 2 !== 0 }"
  >
    <dt
      :id="'health-detail__' + name"
      class="text-sm font-medium text-gray-500"
    >
      {{ name }}
    </dt>
    <dd
      class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2"
      :aria-labelledby="'health-detail__' + name"
    >
      <sba-status-badge v-if="health.status" :status="health.status" />

      <dl v-if="details && details.length > 0" class="grid grid-cols-2 mt-2">
        <template v-for="detail in details" :key="detail.name">
          <dt class="font-medium" v-text="detail.name" />
          <dd
            v-if="name === 'diskSpace'"
            v-text="
              typeof detail.value === 'number'
                ? prettyBytes(detail.value)
                : detail.value
            "
          />
          <dd v-else-if="typeof detail.value === 'object'">
            <pre
              class="break-words whitespace-pre-wrap"
              v-text="toJson(detail.value)"
            />
          </dd>
          <dd
            v-else
            class="break-words whitespace-pre-wrap"
            v-text="detail.value"
          />
        </template>
      </dl>
    </dd>
  </dl>

  <health-details
    v-for="(child, idx) in childHealth"
    :key="child.name"
    :index="idx + 1"
    :name="child.name"
    :health="child.value"
  />
</template>

<script>
import prettyBytes from 'pretty-bytes';

const isChildHealth = (value) => {
  return value !== null && typeof value === 'object' && 'status' in value;
};

export default {
  name: 'HealthDetails',
  props: {
    name: {
      type: String,
      required: true,
    },
    health: {
      type: Object,
      required: true,
    },
    index: {
      type: Number,
      default: 0,
    },
  },
  computed: {
    details() {
      if (this.health.details || this.health.components) {
        return Object.entries(this.health.details || this.health.components)
          .filter(([, value]) => !isChildHealth(value))
          .map(([name, value]) => ({ name, value }));
      }
      return [];
    },
    childHealth() {
      if (this.health.details || this.health.components) {
        return Object.entries(this.health.details || this.health.components)
          .filter(([, value]) => isChildHealth(value))
          .map(([name, value]) => ({ name, value }));
      }
      return [];
    },
  },
  methods: {
    prettyBytes,
    toJson(obj) {
      return JSON.stringify(obj, null, 2);
    },
  },
};
</script>
