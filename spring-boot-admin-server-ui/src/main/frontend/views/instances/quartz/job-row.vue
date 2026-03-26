<template>
  <tbody>
    <!-- Main Job Row -->
    <tr
      :class="{ 'bg-blue-50': isExpanded }"
      class="border-b border-gray-200 hover:bg-gray-50 cursor-pointer"
      @click="toggle"
    >
      <td colspan="5" class="px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <button class="text-gray-600 hover:text-gray-900 transition-colors">
              <font-awesome-icon
                :icon="isExpanded ? 'chevron-down' : 'chevron-right'"
                class="h-4 w-4"
              />
            </button>
            <div>
              <p class="font-semibold text-gray-900">{{ job.name }}</p>
              <p class="text-xs text-gray-500 font-mono">{{ job.className }}</p>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <span
              class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800"
            >
              {{ job.group }}
            </span>
            <span
              v-if="job.durable"
              class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800"
            >
              <font-awesome-icon icon="check-circle" class="mr-1 h-3 w-3" />
              Durable
            </span>
            <span
              v-else
              class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800"
            >
              <font-awesome-icon icon="times-circle" class="mr-1 h-3 w-3" />
              Not Durable
            </span>
            <span
              v-if="job.requestRecovery"
              class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800"
            >
              <font-awesome-icon icon="shield" class="mr-1 h-3 w-3" />
              Recovery
            </span>
          </div>
        </div>
      </td>
    </tr>

    <!-- Expanded Details Row -->
    <tr v-if="isExpanded" class="bg-blue-50 border-b border-gray-200">
      <td colspan="5" class="px-6 py-6">
        <div class="space-y-6">
          <!-- Details Section -->
          <div class="grid grid-cols-2 gap-6">
            <!-- Left Column -->
            <div>
              <h4
                class="mb-4 flex items-center gap-2 font-semibold text-gray-900"
              >
                <font-awesome-icon
                  icon="info-circle"
                  class="h-4 w-4 text-blue-600"
                />
                Details
              </h4>
              <div class="space-y-3 text-sm">
                <div>
                  <p class="font-medium text-gray-700">Job Name</p>
                  <p class="text-gray-600">{{ job.name }}</p>
                </div>
                <div>
                  <p class="font-medium text-gray-700">Class</p>
                  <p class="text-xs text-gray-600 font-mono">
                    {{ job.className }}
                  </p>
                </div>
                <div>
                  <p class="font-medium text-gray-700">Group</p>
                  <p class="text-gray-600">{{ job.group }}</p>
                </div>
                <div v-if="job.description">
                  <p class="font-medium text-gray-700">Description</p>
                  <p class="text-gray-600">{{ job.description }}</p>
                </div>
              </div>
            </div>

            <!-- Right Column -->
            <div>
              <h4
                class="mb-4 flex items-center gap-2 font-semibold text-gray-900"
              >
                <font-awesome-icon icon="cog" class="h-4 w-4 text-blue-600" />
                Configuration
              </h4>
              <div class="space-y-3 text-sm">
                <div>
                  <p class="font-medium text-gray-700">Durable</p>
                  <p class="flex items-center gap-2">
                    <font-awesome-icon
                      :icon="job.durable ? 'check-circle' : 'times-circle'"
                      :class="job.durable ? 'text-green-600' : 'text-red-600'"
                      class="h-4 w-4"
                    />
                    {{ job.durable ? 'Yes' : 'No' }}
                  </p>
                </div>
                <div>
                  <p class="font-medium text-gray-700">Request Recovery</p>
                  <p class="flex items-center gap-2">
                    <font-awesome-icon
                      :icon="
                        job.requestRecovery ? 'check-circle' : 'times-circle'
                      "
                      :class="
                        job.requestRecovery ? 'text-green-600' : 'text-red-600'
                      "
                      class="h-4 w-4"
                    />
                    {{ job.requestRecovery ? 'Yes' : 'No' }}
                  </p>
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
                <font-awesome-icon icon="link" class="h-4 w-4 text-blue-600" />
                Associated Triggers ({{ job.triggers.length }})
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
                Job Data
              </h4>
              <div class="overflow-hidden rounded border border-gray-300">
                <table class="min-w-full text-sm">
                  <thead>
                    <tr class="border-b border-gray-300 bg-gray-100">
                      <th
                        class="px-4 py-2 text-left font-semibold text-gray-900"
                      >
                        Key
                      </th>
                      <th
                        class="px-4 py-2 text-left font-semibold text-gray-900"
                      >
                        Value
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
        </div>
      </td>
    </tr>
  </tbody>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface JobTrigger {
  group: string;
  name: string;
  previousFireTime?: string;
  nextFireTime?: string;
  priority: number;
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
}

defineProps<Props>();

const emit = defineEmits<{
  toggle: [];
}>();

const job = computed(() => {
  // This will be provided via props
  return {} as JobDetail;
});

const triggerKey = (trigger: JobTrigger): string =>
  `${trigger.group}-${trigger.name}`;

const toggle = (): void => {
  emit('toggle');
};
</script>
