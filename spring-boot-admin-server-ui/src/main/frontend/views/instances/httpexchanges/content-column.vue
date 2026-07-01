<script setup lang="ts">
import { faArrowDown, faArrowUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import prettyBytes from 'pretty-bytes';
import { computed } from 'vue';

import { Exchange } from '@/views/instances/httpexchanges/Exchange';

const { data } = defineProps<{
  data: Exchange;
}>();

const hasRequestData = computed(() => {
  return data.contentTypeRequest || data.contentLengthRequest;
});

const hasResponseData = computed(() => {
  return data.contentTypeResponse || data.contentLengthResponse;
});
</script>

<template>
  <div v-if="hasRequestData" class="request flex items-center gap-1">
    <font-awesome-icon :icon="faArrowUp" class="text-xs text-gray-500" />
    <span>{{ data.contentTypeRequest }}</span>
    <span v-if="data.contentLengthRequest" class="text-xs text-gray-500">
      ({{ prettyBytes(data.contentLengthRequest) }})
    </span>
  </div>
  <div v-if="hasResponseData" class="response flex items-center gap-1">
    <font-awesome-icon :icon="faArrowDown" class="text-xs text-gray-500" />
    <span>{{ data.contentTypeResponse }}</span>
    <span v-if="data.contentLengthResponse" class="text-xs text-gray-500">
      ({{ prettyBytes(data.contentLengthResponse) }})
    </span>
  </div>
</template>
