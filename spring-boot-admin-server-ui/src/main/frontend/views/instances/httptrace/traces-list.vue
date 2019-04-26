<!--
  - Copyright 2014-2019 the original author or authors.
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
  <table class="httptraces table is-hoverable is-fullwidth">
    <thead>
      <tr>
        <th class="httptraces__trace-timestamp" v-text="$t('instances.httptrace.timestamp')" />
        <th class="httptraces__trace-method" v-text="$t('instances.httptrace.method')" />
        <th class="httptraces__trace-uri" v-text="$t('instances.httptrace.uri')" />
        <th class="httptraces__trace-status" v-text="$t('instances.httptrace.status')" />
        <th class="httptraces__trace-contentType" v-text="$t('instances.httptrace.content_type')" />
        <th class="httptraces__trace-contentLength" v-text="$t('instances.httptrace.length')" />
        <th class="httptraces__trace-timeTaken" v-text="$t('instances.httptrace.time')" />
      </tr>
    </thead>
    <transition-group tag="tbody" name="fade-in">
      <tr key="new-traces" v-if="newTracesCount > 0">
        <td
          colspan="7"
          class="has-text-primary has-text-centered is-selectable"
          v-text="`${newTracesCount} new traces`"
          @click="$emit('show-new-traces')"
        />
      </tr>
      <template v-for="trace in traces">
        <tr class="is-selectable"
            :class="{ 'httptraces__trace---is-detailed' : showDetails[trace.key] }"
            @click="showDetails[trace.key] ? $delete(showDetails, trace.key) : $set(showDetails, trace.key, true)"
            :key="trace.key"
        >
          <td class="httptraces__trace-timestamp" v-text="trace.timestamp.format('L HH:mm:ss.SSS')" />
          <td class="httptraces__trace-method" v-text="trace.request.method" />
          <td class="httptraces__trace-uri" v-text="trace.request.uri" />
          <td class="httptraces__trace-status">
            <span v-text="trace.response.status" class="tag"
                  :class="{ 'is-success' : trace.isSuccess(), 'is-warning' : trace.isClientError(), 'is-danger' : trace.isServerError() }"
            />
          </td>
          <td class="httptraces__trace-contentType" v-text="trace.contentType" />
          <td class="httptraces__trace-contentLength"
              v-text="trace.contentLength ? prettyBytes(trace.contentLength) : ''"
          />
          <td class="httptraces__trace-timeTaken"
              v-text="trace.timeTaken !== null && typeof trace.timeTaken !== 'undefined' ? `${trace.timeTaken} ms` : ''"
          />
        </tr>
        <tr :key="`${trace.key}-detail`" v-if="showDetails[trace.key]">
          <td colspan="7">
            <pre class="httptraces__trace-detail" v-text="toJson(trace)" />
          </td>
        </tr>
      </template>
      <tr key="no-traces" v-if="traces.length === 0">
        <td class="is-muted" colspan="7" v-text="$t('instances.httptrace.no_traces_found')" />
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
        default: 0
      },
      traces: {
        type: Array,
        default: () => []
      }
    },
    data: () => ({
      showDetails: {}
    }),
    methods: {
      prettyBytes,
      toJson(obj) {
        return JSON.stringify(obj, null, 4);
      }
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .httptraces {
    table-layout: fixed;

    td {
      vertical-align: middle;
      overflow: hidden;
      word-wrap: break-word;
    }

    &__trace {
      &--is-detailed td {
        border: none !important;
      }

      &-timestamp {
        width: 130px;
      }

      &-method {
        width: 100px;
      }

      &-uri {
        width: auto;
      }

      &-status {
        width: 80px;
      }

      &-contentType {
        width: 200px;
      }

      &-contentLength {
        width: 100px;
      }

      &-timeTaken {
        width: 120px;
      }

      &-detail {
        overflow: auto;
      }
    }
  }

</style>

