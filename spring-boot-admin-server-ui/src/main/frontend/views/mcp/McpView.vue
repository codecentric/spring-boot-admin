<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { CATEGORY_COLORS, McpService, type McpTool } from './mcp.service';

import SbaWave from '@/components/sba-wave.vue';

const tools = ref<McpTool[]>([]);
const error = ref<string | null>(null);
const loading = ref(true);

async function loadTools() {
  loading.value = true;
  error.value = null;
  try {
    tools.value = await McpService.fetchTools();
  } catch (e: any) {
    error.value = e?.message ?? String(e);
  } finally {
    loading.value = false;
  }
}

onMounted(loadTools);
</script>

<template>
  <sba-wave />
  <div class="prose prose-slate py-10 mx-20 max-w-none">
    <h1 class="mb-1" v-text="$t('mcp.title')" />
    <p class="text-slate-600 mt-0">
      {{ $t('mcp.description') }}
    </p>

    <!-- Loading -->
    <div v-if="loading" class="not-prose mt-8 text-slate-500 text-sm">
      {{ $t('mcp.loading') }}
    </div>

    <!-- Error -->
    <div
      v-else-if="error"
      class="not-prose mt-8 p-4 border border-red-200 rounded-lg bg-red-50 text-red-700 text-sm"
    >
      <strong>{{ $t('mcp.error') }}:</strong> {{ error }}
    </div>

    <!-- Tools -->
    <div v-else class="not-prose mt-8 grid gap-4">
      <p class="text-sm text-slate-500 mb-2">
        {{ $t('mcp.toolCount', { count: tools.length }) }}
      </p>
      <div
        v-for="tool in tools"
        :key="tool.name"
        class="border border-slate-200 rounded-lg p-4 bg-white shadow-sm hover:shadow-md transition-shadow"
      >
        <div class="flex items-start gap-3">
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 flex-wrap">
              <code
                class="text-sm font-semibold text-slate-900 bg-slate-100 px-2 py-0.5 rounded"
                >{{ tool.name }}</code
              >
              <span
                class="text-xs font-medium px-2 py-0.5 rounded-full"
                :class="
                  CATEGORY_COLORS[McpService.categoryOf(tool.name)] ??
                  CATEGORY_COLORS['other']
                "
              >
                {{ McpService.categoryOf(tool.name) }}
              </span>
            </div>
            <p class="mt-2 text-sm text-slate-600 leading-relaxed">
              {{ tool.description }}
            </p>
            <div v-if="tool.params.length > 0" class="mt-3">
              <table class="w-full text-xs border-collapse">
                <thead>
                  <tr class="border-b border-slate-200">
                    <th
                      class="text-left py-1 pr-3 font-semibold text-slate-700 w-40"
                    >
                      {{ $t('mcp.param') }}
                    </th>
                    <th
                      class="text-left py-1 pr-3 font-semibold text-slate-700 w-16"
                    >
                      {{ $t('mcp.type') }}
                    </th>
                    <th
                      class="text-left py-1 pr-3 font-semibold text-slate-700 w-20"
                    >
                      {{ $t('mcp.required') }}
                    </th>
                    <th class="text-left py-1 font-semibold text-slate-700">
                      {{ $t('mcp.paramDescription') }}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="param in tool.params"
                    :key="param.name"
                    class="border-b border-slate-100 last:border-0"
                  >
                    <td class="py-1 pr-3">
                      <code class="text-slate-800">{{ param.name }}</code>
                    </td>
                    <td class="py-1 pr-3 text-slate-500">{{ param.type }}</td>
                    <td class="py-1 pr-3">
                      <span
                        v-if="param.required"
                        class="text-red-600 font-medium"
                        >{{ $t('mcp.yes') }}</span
                      >
                      <span v-else class="text-slate-400">{{
                        $t('mcp.no')
                      }}</span>
                    </td>
                    <td class="py-1 text-slate-600">{{ param.description }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <p v-else class="mt-2 text-xs text-slate-400 italic">
              {{ $t('mcp.noParams') }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
