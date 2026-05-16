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
    class="px-4 py-3 even:bg-white odd:bg-gray-100"
    role="definition"
    :aria-label="name"
  >
    <dt
      :id="`health-${id}__${name}`"
      class="flex text-sm font-medium text-gray-500 items-center"
    >
      <div class="w-48">
        {{ name }}
      </div>
      <div class="flex-1">
        <sba-status-badge v-if="health.status" :status="health.status" />
      </div>
      <div v-if="details && details.length > 0" class="w-12 text-right">
        <sba-icon-button
          class="p-0!"
          :class="
            classNames({
              'text-sba-600!': !isCollapsed,
              'text-black': isCollapsed,
            })
          "
          :icon="faCircleInfo"
          :title="t('instances.details.health.toggle_details', { name })"
          :aria-label="t('instances.details.health.toggle_details', { name })"
          :aria-expanded="!isCollapsed"
          :aria-controls="`health-details-${id}__${name}`"
          @click="() => toggleCollapsed()"
        />
      </div>
    </dt>
    <dd
      :id="`health-details-${id}__${name}`"
      class="mt-1 text-sm text-gray-900 sm:mt-0"
      :aria-labelledby="`health-${id}__` + name"
    >
      <dl v-if="!isCollapsed" class="grid grid-cols-6 mt-2">
        <template v-for="detail in details" :key="detail.name">
          <dt
            :id="`health-detail-${id}__${detail.name}`"
            class="font-medium col-span-2"
            v-text="detail.name"
          />
          <dd
            v-if="
              name.toLowerCase().startsWith('diskspace') &&
              typeof detail.value === 'number'
            "
            class="col-span-4"
            role="definition"
            :aria-label="detail.name"
            :aria-labelledby="`health-detail-${id}__${detail.name}`"
            v-text="prettyBytes(detail.value)"
          />
          <dd
            v-else-if="typeof detail.value === 'object'"
            class="col-span-4"
            role="definition"
            :aria-label="detail.name"
            :aria-labelledby="`health-detail-${id}__${detail.name}`"
          >
            <sba-formatted-obj
              class="overflow-auto whitespace-pre!"
              :value="detail.value"
            />
          </dd>
          <dd
            v-else
            role="definition"
            :aria-label="detail.name"
            :aria-labelledby="`health-detail-${id}__${detail.name}`"
            class="wrap-break-word whitespace-pre-wrap col-span-4"
            v-html="autolink(detail.value)"
          />
        </template>
      </dl>
    </dd>
  </dl>
  <health-details
    v-for="child in childHealth"
    :key="child.name"
    :instance="instance"
    :name="child.name"
    :health="child.value"
  />
</template>

<script lang="ts" setup>
import { faCircleInfo } from '@fortawesome/free-solid-svg-icons';
import classNames from 'classnames';
import prettyBytes from 'pretty-bytes';
import { computed, onMounted, ref, useId } from 'vue';
import { useI18n } from 'vue-i18n';

import SbaFormattedObj from '@/components/sba-formatted-obj.vue';
import SbaIconButton from '@/components/sba-icon-button.vue';

import Instance from '@/services/instance';
import autolink from '@/utils/autolink';

const { t } = useI18n();
const id = useId();

const { health, name, instance } = defineProps<{
  instance: Instance;
  name: string;
  health: Record<string, any>;
}>();

const COLLAPSED_KEY = `de.codecentric.spring-boot-admin.health-details.${name}.${instance.id}.collapsed`;
const isCollapsed = ref(true);

onMounted(() => {
  const stored = localStorage.getItem(COLLAPSED_KEY);
  if (stored !== null) {
    isCollapsed.value = stored === 'true';
  }
});

type Details = {
  name: string;
  value: string;
};

const isChildHealth = (value: any) => {
  return value !== null && typeof value === 'object' && 'status' in value;
};

const details = computed(() => {
  if (health.details || health.components) {
    return Object.entries(health.details || health.components)
      .filter(([, value]) => !isChildHealth(value))
      .map(([name, value]) => ({ name, value }) as Details);
  }
  return [];
});

const childHealth = computed(() => {
  if (health.details || health.components) {
    return Object.entries(health.details || health.components)
      .filter(([, value]) => isChildHealth(value))
      .map(([name, value]) => ({ name, value }));
  }
  return [];
});

const toggleCollapsed = () => {
  isCollapsed.value = !isCollapsed.value;
  localStorage.setItem(COLLAPSED_KEY, JSON.stringify(isCollapsed.value));
};
</script>

<style scoped>
@reference "../../../index.css";
:deep(a[href]) {
  @apply underline;
}
</style>
