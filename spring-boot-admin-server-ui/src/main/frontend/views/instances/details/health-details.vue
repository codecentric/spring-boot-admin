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
  <!-- Composite contributor: a section header row + collapsible indented children -->
  <div
    v-if="isComposite"
    class="health-section-header"
    role="group"
    :aria-label="name"
  >
    <div
      class="health-section-header__row"
      :aria-expanded="String(!isCollapsed)"
      @click="toggleCollapsed"
    >
      <span
        class="health-section-header__indicator"
        :class="`status--${statusClass}`"
      />
      <span class="health-section-header__name">{{ name }}</span>
      <div class="health-section-header__body">
        <sba-status-badge v-if="health.status" :status="health.status" />
      </div>
      <font-awesome-icon
        :icon="faChevronRight"
        class="health-section-header__chevron transition-transform px-4"
        :class="{ 'rotate-90': !isCollapsed }"
      />
    </div>
  </div>

  <!-- Children of composite: indented, rendered as DOM siblings (outside role="group") -->
  <template v-if="isComposite && !isCollapsed">
    <div class="health-section-children">
      <health-details
        v-for="(child, idx) in childHealth"
        :key="`${child.name}_${idx}`"
        :instance="instance"
        :name="child.name"
        :health="child.value"
      />
    </div>
  </template>

  <!-- Leaf contributor: a single row with expandable detail grid -->
  <dl
    v-else-if="!isComposite"
    class="health-row"
    role="group"
    :aria-label="name"
  >
    <div class="health-row__indicator" :class="`status--${statusClass}`" />

    <dt :id="`health-${id}__${safeNameId}`" class="health-row__name">
      {{ name }}
    </dt>

    <dd
      :id="`health-details-${id}__${safeNameId}`"
      class="health-row__body"
      :aria-labelledby="`health-${id}__${safeNameId}`"
    >
      <div class="health-row__summary">
        <sba-status-badge v-if="health.status" :status="health.status" />
        <span v-if="health.description" class="health-row__description">
          {{ health.description }}
        </span>
        <button
          v-if="details && details.length > 0"
          class="health-row__toggle"
          :title="t('instances.details.health.toggle_details', { name })"
          :aria-label="t('instances.details.health.toggle_details', { name })"
          :aria-expanded="String(!isCollapsed)"
          :aria-controls="`health-details-${id}__${safeNameId}`"
          @click="toggleCollapsed"
        >
          <font-awesome-icon
            :icon="faChevronRight"
            class="transition-transform"
            :class="{ 'rotate-90': !isCollapsed }"
          />
        </button>
      </div>

      <dl
        v-if="details && details.length > 0"
        v-show="!isCollapsed"
        class="health-row__details"
      >
        <template
          v-for="(detail, idx) in details"
          :key="`${detail.name}_${idx}`"
        >
          <dt
            :id="`health-detail-${id}__${safeDetailId(detail.name, idx)}`"
            class="health-row__detail-key"
            v-text="detail.name"
          />
          <dd
            v-if="
              name.toLowerCase().startsWith('diskspace') &&
              typeof detail.value === 'number'
            "
            class="health-row__detail-value"
            role="definition"
            :aria-label="detail.name"
            :aria-labelledby="`health-detail-${id}__${safeDetailId(detail.name, idx)}`"
            v-text="prettyBytes(detail.value as number)"
          />
          <dd
            v-else-if="
              detail.value !== null && typeof detail.value === 'object'
            "
            class="health-row__detail-value"
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
            class="health-row__detail-value wrap-break-word whitespace-pre-wrap"
            v-html="autolink(String(detail.value ?? ''))"
          />
        </template>
      </dl>
    </dd>
  </dl>
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

const safeNameId = computed(() => (name ?? '').replace(/[^a-zA-Z0-9_-]/g, '_'));

function safeDetailId(detailName: string, idx: number): string {
  const safe = detailName.replace(/[^a-zA-Z0-9_-]/g, '_');
  return safe.length > 0 ? safe : `detail_${idx}`;
}

const statusClass = computed(() =>
  (health?.status ?? 'unknown').toLowerCase().replace(/_/g, '-'),
);

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

const isChildHealth = (value: any) =>
  value !== null && typeof value === 'object' && 'status' in value;

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

const isComposite = computed(
  () => childHealth.value.length > 0 && details.value.length === 0,
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

/* ── Status colour tokens — sourced from theme.css @theme ──────────────── */
.status--up {
  --status-color: var(--color-status-up);
}
.status--down,
.status--offline,
.status--out-of-service {
  --status-color: var(--color-status-down);
}
.status--restricted {
  --status-color: var(--color-status-restricted);
}
.status--unknown {
  --status-color: var(--color-status-unknown);
}

/* ── Composite section header (flat, no card chrome) ───────────────────── */
.health-section-header {
  @apply border-b border-gray-100 bg-gray-50 last:border-b-0;
}

.health-section-header__row {
  @apply w-full flex items-stretch text-left cursor-pointer select-none
         hover:bg-gray-100 transition-colors;
}

.health-section-header__indicator {
  @apply w-1 shrink-0;
  background-color: var(--status-color, var(--color-status-unknown));
}

.health-section-header__name {
  @apply w-52 shrink-0 px-4 py-2.5 text-xs font-semibold text-gray-500
         tracking-wide break-all self-center;
}

.health-section-header__body {
  @apply flex-1 py-2.5 min-w-0 self-center;
}

.health-section-header__chevron {
  @apply text-gray-400 self-center;
}

/* ── Children of composite: indented text, flush indicator bar ─────────── */
.health-section-children {
  @apply border-b border-gray-100 divide-y divide-gray-100;
}

.health-section-children :deep(.health-row__name),
.health-section-children :deep(.health-section-header__name) {
  @apply pl-8;
}

/* ── Leaf row ──────────────────────────────────────────────────────────── */
.health-row {
  @apply flex items-stretch border-b border-gray-100 last:border-b-0
         even:bg-white odd:bg-gray-50;
}

.health-row__indicator {
  @apply w-1 self-stretch shrink-0;
  background-color: var(--status-color, var(--color-status-unknown));
}

.health-row__name {
  @apply w-52 shrink-0 px-4 pt-[calc(theme(spacing.3)+theme(spacing.1))] pb-3
         text-sm font-medium text-gray-600 break-all self-start;
}

.health-row__body {
  @apply flex-1 pr-4 py-3 min-w-0 self-start;
}

.health-row__summary {
  @apply flex items-center gap-2 flex-wrap;
}

.health-row__description {
  @apply text-xs text-gray-500 italic;
}

.health-row__toggle {
  @apply ml-auto text-gray-400 hover:text-gray-600 transition-colors
         border-none bg-transparent cursor-pointer rounded;
}

/* ── Detail grid inside a leaf row ────────────────────────────────────── */
.health-row__details {
  @apply mt-2 grid grid-cols-6 gap-x-2 gap-y-1 text-sm;
}

.health-row__detail-key {
  @apply col-span-2 font-medium text-gray-500 break-all;
}

.health-row__detail-value {
  @apply col-span-4 text-gray-800 break-all;
}

:deep(a[href]) {
  @apply underline;
}
</style>
