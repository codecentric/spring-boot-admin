<!--
  - Copyright 2014-2018 the original author or authors.
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
    <table class="table is-hoverable">
        <thead>
        <tr>
            <th>Timestamp</th>
            <th>Method</th>
            <th>Path</th>
            <th>Status</th>
            <th>Content-Type</th>
            <th>Content-Length</th>
            <th>Time</th>
        </tr>
        </thead>
        <template v-for="trace in traces">
            <tr class="is-selectable"
                :class="{ 'trace--is-detailed' : showDetails[trace.key] }"
                @click="showDetails[trace.key]  ?  $delete(showDetails, trace.key) : $set(showDetails, trace.key, true)"
                :key="trace.key">
                <td v-text="trace.timestamp.format('L HH:mm:ss.SSS')"></td>
                <td v-text="trace.method"></td>
                <td v-text="trace.path"></td>
                <td>
                    <span v-text="trace.status" class="tag"
                          :class="{ 'is-success' : trace.isSuccess(), 'is-warning' : trace.isClientError(), 'is-danger' : trace.isServerError() }"></span>
                </td>
                <td v-text="trace.contentType"></td>
                <td v-text="trace.contentLength ? prettyBytes(trace.contentLength) : ''"></td>
                <td v-text="trace.timeTaken !== null && typeof trace.timeTaken !== 'undefined' ? `${trace.timeTaken} ms` : ''"></td>
            </tr>
            <tr class="trace__detail" :key="`${trace.key}-detail`" v-if="showDetails[trace.key]">
                <td colspan="7">
                    <pre v-text="toJson(trace.info)"></pre>
                </td>
            </tr>
        </template>
        <tr v-if="traces.length === 0">
            <td class="is-muted" colspan="5">No traces found.</td>
        </tr>
    </table>
</template>

<script>
  import prettyBytes from 'pretty-bytes';

  export default {
    props: ['traces'],
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

    .trace--is-detailed td {
        border: none !important;
    }

    .trace__detail td {
        overflow-x: auto;
        max-width: 1024px;
    }
</style>