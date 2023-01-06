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
  <table class="httptraces table table-full table-sm">
    <thead>
      <tr>
        <th
          class="httptraces__trace-timestamp"
          v-text="$t('instances.httptrace.timestamp')"
        />
        <th
          class="httptraces__trace-method"
          v-text="$t('instances.httptrace.method')"
        />
        <th
          class="httptraces__trace-uri"
          v-text="$t('instances.httptrace.uri')"
        />
        <th
          class="httptraces__trace-status"
          v-text="$t('instances.httptrace.status')"
        />
        <th
          class="httptraces__trace-contentType"
          v-text="$t('instances.httptrace.content_type_request')"
        />
        <th
          class="httptraces__trace-contentLength"
          v-text="$t('instances.httptrace.length_request')"
        />
        <th
          class="httptraces__trace-contentType"
          v-text="$t('instances.httptrace.content_type_response')"
        />
        <th
          class="httptraces__trace-contentLength"
          v-text="$t('instances.httptrace.length_response')"
        />
        <th
          class="httptraces__trace-timeTaken"
          v-text="$t('instances.httptrace.time')"
        />
      </tr>
    </thead>
    <tbody>
      <tr v-if="newTracesCount > 0" key="new-traces">
        <td
          colspan="9"
          class="text-center"
          @click="$emit('show-new-traces')"
          v-text="`${newTracesCount} new traces`"
        />
      </tr>
    </tbody>
    <transition-group tag="tbody" name="fade-in">
      <template v-for="trace in traces" :key="trace.key">
        <tr
          class="is-selectable"
          :class="{ 'httptraces__trace---is-detailed': showDetails[trace.key] }"
          @click="
            showDetails[trace.key]
              ? delete showDetails[trace.key]
              : (showDetails[trace.key] = true)
          "
        >
          <td
            class="httptraces__trace-timestamp"
            v-text="trace.timestamp.format('L HH:mm:ss.SSS')"
          />
          <td class="httptraces__trace-method" v-text="trace.request.method" />
          <td class="httptraces__trace-uri" v-text="trace.request.uri" />
          <td class="httptraces__trace-status">
            <span
              class="tag"
              :class="{
                'is-muted': trace.isPending(),
                'is-success': trace.isSuccess(),
                'is-warning': trace.isClientError(),
                'is-danger': trace.isServerError(),
              }"
              v-text="trace.response ? trace.response.status : 'pending'"
            />
          </td>
          <td
            class="httptraces__trace-contentType"
            v-text="trace.contentTypeRequest"
          />
          <td
            class="httptraces__trace-contentLength"
            v-text="
              trace.contentLengthRequest
                ? prettyBytes(trace.contentLengthRequest)
                : ''
            "
          />
          <td
            class="httptraces__trace-contentType"
            v-text="trace.contentTypeResponse"
          />
          <td
            class="httptraces__trace-contentLength"
            v-text="
              trace.contentLengthResponse
                ? prettyBytes(trace.contentLengthResponse)
                : ''
            "
          />
          <td
            class="httptraces__trace-timeTaken"
            v-text="
              trace.timeTaken !== null && typeof trace.timeTaken !== 'undefined'
                ? `${trace.timeTaken} ms`
                : ''
            "
          />
        </tr>
        <tr v-if="showDetails[trace.key]" :key="`${trace.key}-detail`">
          <td colspan="7">
            <pre class="httptraces__trace-detail" v-text="toJson(trace)" />
          </td>
        </tr>
      </template>
      <tr v-if="traces.length === 0" key="no-traces">
        <td
          class="is-muted"
          colspan="7"
          v-text="$t('instances.httptrace.no_traces_found')"
        />
      </tr>
    </transition-group>
  </table>
</template>

<script>
import prettyBytes from 'pretty-bytes';

export default {
  props: {
    newTracesCount: {
      type: Number,
      default: 0,
    },
    traces: {
      type: Array,
      default: () => [],
    },
  },
  emits: ['show-new-traces'],
  data: () => ({
    showDetails: {},
  }),
  methods: {
    prettyBytes,
    toJson(obj) {
      return JSON.stringify(obj, null, 4);
    },
  },
};
</script>

<style lang="css">
.httptraces {
  table-layout: fixed;
}
.httptraces td {
  vertical-align: middle;
  overflow: hidden;
  word-wrap: break-word;
}
.httptraces__trace--is-detailed td {
  border: none !important;
}
.httptraces__trace-timestamp {
  width: 130px;
}
.httptraces__trace-method {
  @apply font-mono;
  width: 100px;
}
.httptraces__trace-uri {
  width: auto;
}
.httptraces__trace-status {
  @apply font-mono;
  width: 80px;
}
.httptraces__trace-contentType {
  width: 200px;
}
.httptraces__trace-contentLength {
  width: 100px;
}
.httptraces__trace-timeTaken {
  width: 120px;
}
.httptraces__trace-detail {
  overflow: auto;
}
</style>
