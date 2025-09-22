<!--
  - Copyright 2014-2024 the original author or authors.
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
  <section
    class="grid grid-cols-2 md:grid-cols-[26.5rem_1fr] items-center w-full"
  >
    <div class="flex" style="grid-area: 1 / 1 / 1 / 3">
      <template v-if="instance.showUrl()">
        <div
          v-tooltip.top="{
            disabled: instanceUrlToShow.length <= 50,
            value: instanceUrlToShow,
            pt: {},
          }"
          class="text-ellipsis overflow-hidden whitespace-nowrap"
          v-text="instanceUrlToShow"
        />
        <div class="ml-1 flex gap-1 items-start">
          <sba-tag
            v-if="instance.registration.metadata?.['group']"
            class="ml-2"
            :value="instance.registration.metadata?.['group']"
            small
          />

          <template v-if="!instance.isUrlDisabled()">
            <sba-button
              as="a"
              :href="instance.registration.serviceUrl"
              size="2xs"
              referrerpolicy="no-referrer"
              target="_blank"
              :aria-label="t('term.homepage')"
            >
              <font-awesome-icon :icon="faHome" size="xs" />
            </sba-button>
          </template>
          <sba-button
            as="a"
            :href="instance.registration.managementUrl"
            size="2xs"
            referrerpolicy="no-referrer"
            target="_blank"
            :aria-label="t('term.actuator_endpoint')"
          >
            <font-awesome-icon :icon="faClipboardList" size="xs" />
          </sba-button>
          <sba-button
            as="a"
            :href="instance.registration.healthUrl"
            size="2xs"
            referrerpolicy="no-referrer"
            target="_blank"
            :aria-label="t('health.label')"
          >
            <font-awesome-icon :icon="faHeart" size="xs" />
          </sba-button>
        </div>
      </template>

      <template v-else>
        <sba-tag
          v-if="instance.registration.metadata?.['group']"
          class="ml-2"
          :value="instance.registration.metadata?.['group']"
          small
        />
      </template>
    </div>

    <span class="instance-id" v-text="instance.id" />
    <span
      class="instance-version text-right lg:text-left"
      v-text="instance.buildVersion"
    />
  </section>
</template>

<script setup lang="ts">
import {
  faClipboardList,
  faHeart,
  faHome,
} from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { useI18n } from 'vue-i18n';

import SbaButton from '@/components/sba-button.vue';
import SbaTag from '@/components/sba-tag.vue';

import Instance from '@/services/instance';

const { t } = useI18n();

const { instance } = defineProps<{
  instance: Instance;
}>();

const instanceUrlToShow =
  instance.registration.serviceUrl || instance.registration.healthUrl;
</script>

<style>
.p-tooltip {
  max-width: fit-content !important;
}
</style>
