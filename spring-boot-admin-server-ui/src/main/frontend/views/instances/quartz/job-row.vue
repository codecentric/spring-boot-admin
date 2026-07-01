<template>
  <!-- Main Job Row -->
  <tr
    :class="{ 'bg-gray-50': isExpanded }"
    class="border-b border-gray-200 bg-white hover:bg-gray-50 cursor-pointer block sm:table-row"
    @click="toggle"
  >
    <td class="px-3 sm:px-6 py-4 block sm:table-cell mb-2 sm:mb-0">
      <div class="flex items-center gap-3 min-w-max">
        <button class="text-gray-600 hover:text-gray-900 transition-colors">
          <font-awesome-icon
            :icon="isExpanded ? 'chevron-down' : 'chevron-right'"
            class="h-4 w-4"
          />
        </button>
        <div>
          <p class="font-semibold text-gray-900 text-sm sm:text-base">
            {{ job.name }}
          </p>
          <p class="text-xs text-gray-500 font-mono break-all">
            {{ job.className }}
          </p>
        </div>
      </div>
    </td>
    <td
      class="px-3 sm:px-6 py-4 text-xs sm:text-sm text-gray-600 block sm:table-cell mb-2 sm:mb-0"
    >
      <span class="line-clamp-2">{{ job.description || '—' }}</span>
    </td>
    <td class="px-3 sm:px-6 py-4 block sm:table-cell mb-2 sm:mb-0">
      <span
        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 whitespace-nowrap"
      >
        {{ job.group }}
      </span>
    </td>
    <td class="px-3 sm:px-6 py-4 block sm:table-cell mb-2 sm:mb-0">
      <span
        v-if="job.durable"
        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 whitespace-nowrap"
      >
        <font-awesome-icon icon="check-circle" class="mr-1 h-3 w-3" />
        {{ $t('instances.quartz.yes') }}
      </span>
      <span
        v-else
        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800 whitespace-nowrap"
      >
        <font-awesome-icon icon="times-circle" class="mr-1 h-3 w-3" />
        {{ $t('instances.quartz.no') }}
      </span>
    </td>
    <td class="px-3 sm:px-6 py-4 block sm:table-cell">
      <span
        v-if="job.requestRecovery"
        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800"
      >
        <font-awesome-icon icon="shield" class="mr-1 h-3 w-3" />
        {{ $t('instances.quartz.enabled') }}
      </span>
      <span
        v-else
        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800"
      >
        {{ $t('instances.quartz.disabled') }}
      </span>
    </td>
  </tr>

  <!-- Expanded Details Row -->
  <tr
    v-if="isExpanded"
    class="bg-gray-50 border-b border-gray-200 block sm:table-row"
  >
    <td colspan="5" class="px-0 py-0 block sm:table-cell">
      <div class="space-y-6 px-3 sm:px-6 py-6">
        <!-- Details Section -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 w-full">
          <!-- Left Column -->
          <div>
            <h4
              class="mb-4 flex items-center gap-2 font-semibold text-gray-900"
            >
              <font-awesome-icon icon="info-circle" class="h-4 w-4" />
              {{ $t('instances.quartz.details') }}
            </h4>
            <div class="space-y-3 text-sm">
              <div>
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.job_name') }}
                </p>
                <p class="text-gray-600">{{ job.name }}</p>
              </div>
              <div>
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.class') }}
                </p>
                <p class="text-xs text-gray-600 font-mono">
                  {{ job.className }}
                </p>
              </div>
              <div>
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.group') }}
                </p>
                <p class="text-gray-600">{{ job.group }}</p>
              </div>
              <div v-if="job.description">
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.description') }}
                </p>
                <p class="text-gray-600">{{ job.description }}</p>
              </div>
            </div>
          </div>

          <!-- Right Column -->
          <div>
            <h4
              class="mb-4 flex items-center gap-2 font-semibold text-gray-900"
            >
              <font-awesome-icon icon="cog" class="h-4 w-4" />
              {{ $t('instances.quartz.configuration') }}
            </h4>
            <div class="space-y-3 text-sm">
              <div>
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.durable') }}
                </p>
                <p class="flex items-center gap-2">
                  <font-awesome-icon
                    :icon="job.durable ? 'check-circle' : 'times-circle'"
                    class="h-4 w-4"
                  />
                  {{
                    job.durable
                      ? $t('instances.quartz.yes')
                      : $t('instances.quartz.no')
                  }}
                </p>
              </div>
              <div>
                <p class="font-medium text-gray-700">
                  {{ $t('instances.quartz.request_recovery') }}
                </p>
                <p class="flex items-center gap-2">
                  <font-awesome-icon
                    :icon="
                      job.requestRecovery ? 'check-circle' : 'times-circle'
                    "
                    class="h-4 w-4"
                  />
                  {{
                    job.requestRecovery
                      ? $t('instances.quartz.yes')
                      : $t('instances.quartz.no')
                  }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Fire Times Section -->
        <div
          v-if="
            job.triggers &&
            job.triggers.some((t) => t.previousFireTime || t.nextFireTime)
          "
        >
          <div class="border-t border-gray-300 pt-4">
            <h4
              class="mb-3 flex items-center gap-2 font-semibold text-gray-900"
            >
              <font-awesome-icon icon="clock" class="h-4 w-4" />
              {{ $t('instances.quartz.trigger_fire_times') }}
            </h4>
            <div class="space-y-2 text-sm">
              <div v-for="trigger in job.triggers" :key="triggerKey(trigger)">
                <p class="font-medium text-gray-700">
                  {{ trigger.name }} ({{ trigger.group }})
                </p>
                <div class="ml-2 space-y-1 text-xs">
                  <p v-if="trigger.previousFireTime" class="text-gray-600">
                    <span class="font-medium"
                      >{{ $t('instances.quartz.last_fire') }}:</span
                    >
                    {{ formatDateTime(trigger.previousFireTime) }}
                  </p>
                  <p v-if="trigger.nextFireTime" class="text-gray-700">
                    <span class="font-medium"
                      >{{ $t('instances.quartz.next_fire') }}:</span
                    >
                    {{ formatDateTime(trigger.nextFireTime) }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Associated Triggers -->
        <div v-if="job.triggers && job.triggers.length > 0">
          <div class="border-t border-gray-300 pt-4">
            <h4
              class="mb-3 flex items-center gap-2 font-semibold text-gray-900"
            >
              <font-awesome-icon icon="link" class="h-4 w-4" />
              {{ $t('instances.quartz.associated_triggers') }} ({{
                job.triggers.length
              }})
            </h4>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="trigger in job.triggers"
                :key="triggerKey(trigger)"
                class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
              >
                {{ trigger.name }}
                <span class="ml-1 opacity-75">({{ trigger.group }})</span>
              </span>
            </div>
          </div>
        </div>

        <!-- Job Data -->
        <div v-if="job.data && Object.keys(job.data).length > 0">
          <div class="border-t border-gray-300 pt-4">
            <h4
              class="mb-3 flex items-center gap-2 font-semibold text-gray-900"
            >
              <font-awesome-icon
                icon="database"
                class="h-4 w-4 text-blue-600"
              />
              {{ $t('instances.quartz.job_data') }}
            </h4>
            <div class="overflow-hidden rounded border border-gray-300">
              <table class="min-w-full text-sm">
                <thead>
                  <tr class="border-b border-gray-300 bg-gray-100">
                    <th class="px-4 py-2 text-left font-semibold text-gray-900">
                      {{ $t('instances.quartz.key') }}
                    </th>
                    <th class="px-4 py-2 text-left font-semibold text-gray-900">
                      {{ $t('instances.quartz.value') }}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="(value, key) in job.data"
                    :key="key"
                    class="border-b border-gray-200 hover:bg-gray-100"
                  >
                    <td class="px-4 py-2 font-medium text-gray-900">
                      {{ key }}
                    </td>
                    <td class="px-4 py-2 text-gray-600 font-mono text-xs">
                      {{ value }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Action Buttons Section -->
        <div v-if="instance" class="border-t border-gray-200 pt-6">
          <div class="flex flex-col gap-3">
            <p class="text-sm font-semibold text-gray-900">
              {{ $t('instances.quartz.actions') }}
            </p>
            <div class="flex flex-wrap gap-2">
              <button
                :disabled="isLoading"
                class="inline-flex items-center gap-2 px-3 py-2 rounded text-sm font-medium bg-blue-100 text-blue-800 hover:bg-blue-200 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                @click.stop="triggerJobNow"
              >
                <font-awesome-icon icon="play-circle" class="h-4 w-4" />
                {{ $t('instances.quartz.trigger_job_now') }}
              </button>
            </div>
            <div
              v-if="actionError"
              class="p-3 bg-gray-50 border border-gray-200 rounded text-sm text-gray-800"
            >
              {{ actionError }}
            </div>
          </div>
        </div>
      </div>
    </td>
  </tr>
</template>

<script setup lang="ts">
import { useNotificationCenter } from '@stekoe/vue-toast-notificationcenter';
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';

import Instance from '@/services/instance';
import { QuartzActuatorService } from '@/services/quartz-actuator';

const notificationCenter = useNotificationCenter();
const { t } = useI18n();

interface JobTrigger {
  group: string;
  name: string;
  previousFireTime?: string;
  nextFireTime?: string;
  priority: number;
  [key: string]: unknown;
}

interface JobDetail {
  group: string;
  name: string;
  className: string;
  description?: string;
  durable: boolean;
  requestRecovery: boolean;
  data?: Record<string, string>;
  triggers: JobTrigger[];
  [key: string]: unknown;
}

interface Props {
  jobDetail: JobDetail;
  isExpanded: boolean;
  instance: Instance;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  toggle: [];
  action: [action: string, success: boolean];
}>();

const isLoading = ref(false);
const actionError = ref<string | null>(null);

const job = computed(() => props.jobDetail);

const triggerKey = (trigger: JobTrigger): string =>
  `${trigger.group}-${trigger.name}`;

const formatDateTime = (dateString?: string): string => {
  if (!dateString) return '—';
  try {
    const date = new Date(dateString);
    return date.toLocaleString();
  } catch {
    return dateString;
  }
};

const toggle = (): void => {
  emit('toggle');
};

const triggerJobNow = async (): Promise<void> => {
  if (!props.instance) return;
  isLoading.value = true;
  actionError.value = null;

  try {
    await QuartzActuatorService.triggerJob(
      props.instance,
      job.value.group,
      job.value.name,
    );
    notificationCenter.success(
      t('instances.quartz.trigger_success_message', {
        jobName: job.value.name,
      }),
      {
        title: t('instances.quartz.trigger_success_title'),
        timeout: 3000,
      },
    );
    emit('action', 'trigger', true);
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : String(error);
    actionError.value = t('instances.quartz.trigger_failed_message', {
      error: errorMessage,
    });
    notificationCenter.error(
      t('instances.quartz.trigger_failed_notification', {
        jobName: job.value.name,
        error: errorMessage,
      }),
      {
        title: t('instances.quartz.trigger_failed'),
        timeout: 5000,
      },
    );
    emit('action', 'trigger', false);
  } finally {
    isLoading.value = false;
  }
};
</script>
