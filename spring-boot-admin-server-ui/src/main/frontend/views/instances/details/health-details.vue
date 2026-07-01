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
    class="px-4 py-3 even:bg-white odd:bg-gray-50 grid grid-cols-3"
    role="group"
    :aria-label="name"
  >
    <dt
      :id="`health-${id}__${safeNameId}`"
      class="w-48 text-sm font-medium text-gray-500 items-center"
    >
      <div class="break-all">
        {{ name }}
      </div>
    </dt>
    <dd
      :id="`health-details-${id}__${safeNameId}`"
      class="mt-1 text-sm text-gray-900 sm:mt-0 col-span-2"
      :aria-labelledby="`health-${id}__${safeNameId}`"
    >
      <div class="flex gap-1 items-center">
        <div class="flex-1">
          <sba-status-badge v-if="health.status" :status="health.status" />
        </div>
        <div v-if="details && details.length > 0" class="w-12 text-right">
          <sba-button
            class="p-0! border-none"
            :title="t('instances.details.health.toggle_details', { name })"
            :aria-label="t('instances.details.health.toggle_details', { name })"
            :aria-expanded="String(!isCollapsed)"
            :aria-controls="`health-details-${id}__${safeNameId}`"
            @click="() => toggleCollapsed()"
          >
            <font-awesome-icon
              :icon="faChevronRight"
              class="transition-transform"
              :class="{ 'rotate-90': !isCollapsed }"
            />
          </sba-button>
        </div>
      </div>
      <dl
        v-if="details && details.length > 0"
        v-show="!isCollapsed"
        class="grid grid-cols-6 mt-2"
      >
        <template
          v-for="(detail, idx) in details"
          :key="`${detail.name}_${idx}`"
        >
          <dt
            :id="`health-detail-${id}__${safeDetailId(detail.name, idx)}`"
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
            :aria-labelledby="`health-detail-${id}__${safeDetailId(detail.name, idx)}`"
            v-text="prettyBytes(detail.value as number)"
          />
          <dd
            v-else-if="
              detail.value !== null && typeof detail.value === 'object'
            "
            class="col-span-4"
            role="definition"
            :aria-label="detail.name"
            :aria-labelledby="`health-detail-${id}__${safeDetailId(detail.name, idx)}`"
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
            :aria-labelledby="`health-detail-${id}__${safeDetailId(detail.name, idx)}`"
            class="wrap-break-word whitespace-pre-wrap col-span-4"
            v-html="autolink(String(detail.value ?? ''))"
          />
        </template>
      </dl>
    </dd>
  </dl>
  <health-details
    v-for="(child, idx) in childHealth"
    :key="`${child.name}_${idx}`"
    :instance="instance"
    :name="child.name"
    :health="child.value"
  />
</template>

<script lang="ts" setup>
import { faChevronRight } from '@fortawesome/free-solid-svg-icons';
import prettyBytes from 'pretty-bytes';
import { computed, ref, useId, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import SbaFormattedObj from '@/components/sba-formatted-obj.vue';

import Instance from '@/services/instance';
import autolink from '@/utils/autolink';

const { t } = useI18n();
const id = useId();

const { health, name, instance } = defineProps<{
  instance: Instance;
  name: string;
  health: Record<string, any>;
}>();

// Sanitised name safe for use in HTML id attributes
const safeNameId = computed(() => (name ?? '').replace(/[^a-zA-Z0-9_-]/g, '_'));

// Sanitise a detail key for use in HTML ids; fall back to its index when result would be empty
function safeDetailId(detailName: string, idx: number): string {
  const safe = detailName.replace(/[^a-zA-Z0-9_-]/g, '_');
  return safe.length > 0 ? safe : `detail_${idx}`;
}

const COLLAPSED_KEY = computed(
  () =>
    `de.codecentric.spring-boot-admin.health-details.${encodeURIComponent(name ?? '')}.${encodeURIComponent(instance?.id ?? '')}.collapsed`,
);

function readCollapsedFromStorage(): boolean {
  if (!instance?.id) return false;
  try {
    const stored = localStorage.getItem(COLLAPSED_KEY.value);
    if (stored !== null) return stored === 'true';
  } catch {
    // storage unavailable — fall back to default
  }
  return false;
}

const isCollapsed = ref(readCollapsedFromStorage());

type Details = {
  name: string;
  value: unknown;
};

const isChildHealth = (value: any) => {
  return value !== null && typeof value === 'object' && 'status' in value;
};

const healthEntries = computed(() => {
  const source = health.details ?? health.components;
  if (source && typeof source === 'object' && !Array.isArray(source)) {
    return Object.entries(source);
  }
  return [];
});

const details = computed(() =>
  healthEntries.value
    .filter(([, value]) => !isChildHealth(value))
    .map(([name, value]) => ({ name, value }) as Details),
);

const childHealth = computed(() =>
  healthEntries.value
    .filter(([, value]) => isChildHealth(value))
    .map(([name, value]) => ({ name, value })),
);

watch(COLLAPSED_KEY, () => {
  isCollapsed.value = readCollapsedFromStorage();
});

const toggleCollapsed = () => {
  const next = !isCollapsed.value;
  if (instance?.id) {
    try {
      localStorage.setItem(COLLAPSED_KEY.value, JSON.stringify(next));
    } catch {
      // storage unavailable — in-memory state still updated
    }
  }
  isCollapsed.value = next;
};
</script>

<style scoped>
@reference "../../../index.css";
:deep(a[href]) {
  @apply underline;
}
</style>
