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
  <ul>
    <li
      v-for="instance in instances"
      :key="instance.id"
      :data-testid="instance.id"
      class="flex p-2 sm:pr-6 hover:bg-gray-100 gap-2 odd:bg-gray-50 items-center"
      @click.stop="showDetails(instance)"
    >
      <div class="pt-1 md:w-16 text-center">
        <sba-status
          :date="instance.statusTimestamp"
          :status="instance.statusInfo.status"
        />
      </div>
      <div class="flex-1 overflow-hidden">
        <section class="flex gap-2 items-center">
          <div class="w-80" data-name="">
            <!-- Section when URL is visible -->
            <template v-if="instance.showUrl()">
              <div class="flex gap-1">
                <div
                  class="overflow-hidden text-ellipsis"
                  v-text="
                    instance.registration.serviceUrl ||
                    instance.registration.healthUrl
                  "
                />
                <div class="ml-1 inline-flex gap-1">
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
              </div>
              <sba-tag
                v-if="instance.registration.metadata?.['group']"
                class="ml-2"
                :value="instance.registration.metadata?.['group']"
                small
              />
              <span class="text-sm italic" v-text="instance.id" />
            </template>
            <!-- URL are hidden -->
            <template v-else>
              <div v-text="instance.id"></div>
              <sba-tag
                v-if="instance.registration.metadata?.['group']"
                class="ml-2"
                :value="instance.registration.metadata?.['group']"
                small
              />
            </template>
          </div>
          <div class="flex-1 hidden lg:block" v-text="instance.buildVersion" />
          <div class="hidden lg:block text-right">
            <slot :instance="instance" name="actions" />
          </div>
        </section>
        <section
          v-if="Object.keys(instance.tags ?? {})?.length > 0"
          class="mt-2 hidden lg:block overflow-x-auto"
        >
          <sba-tags :small="true" :tags="instance.tags" />
        </section>
      </div>
    </li>
  </ul>
</template>

<script lang="ts" setup>
import {
  faClipboardList,
  faHeart,
  faHome,
} from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { PropType } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';

import SbaButton from '@/components/sba-button.vue';
import SbaStatus from '@/components/sba-status.vue';
import SbaTag from '@/components/sba-tag.vue';
import SbaTags from '@/components/sba-tags.vue';

import Instance from '@/services/instance';

const router = useRouter();
const { t } = useI18n();

defineProps({
  instances: {
    type: Object as PropType<Array<Instance>>,
    default: () => [] as Instance[],
  },
  showNotificationSettings: {
    type: Boolean,
    default: false,
  },
  hasActiveNotificationFilter: {
    type: Function,
    default: () => false,
  },
});

const showDetails = (instance: Instance) => {
  router.push({
    name: 'instances/details',
    params: { instanceId: instance.id },
  });
};
</script>

<style lang="css">
.instances-list td {
  vertical-align: middle;
}
</style>
