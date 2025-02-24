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
  <table class="httpexchanges table table-full table-sm">
    <thead>
      <tr>
        <th
          class="httpexchanges__exchange-timestamp"
          v-text="$t('instances.httpexchanges.timestamp')"
        />
        <th
          class="httpexchanges__exchange-method"
          v-text="$t('instances.httpexchanges.method')"
        />
        <th
          class="httpexchanges__exchange-uri"
          v-text="$t('instances.httpexchanges.uri')"
        />
        <th
          class="httpexchanges__exchange-status"
          v-text="$t('instances.httpexchanges.status')"
        />
        <th
          class="httpexchanges__exchange-contentType"
          v-text="$t('instances.httpexchanges.content_type_request')"
        />
        <th
          class="httpexchanges__exchange-contentLength"
          v-text="$t('instances.httpexchanges.length_request')"
        />
        <th
          class="httpexchanges__exchange-contentType"
          v-text="$t('instances.httpexchanges.content_type_response')"
        />
        <th
          class="httpexchanges__exchange-contentLength"
          v-text="$t('instances.httpexchanges.length_response')"
        />
        <th
          class="httpexchanges__exchange-timeTaken"
          v-text="$t('instances.httpexchanges.time')"
        />
      </tr>
    </thead>
    <tbody>
      <tr v-if="newExchangesCount > 0" key="new-exchanges">
        <td
          colspan="9"
          class="text-center"
          @click="$emit('show-new-exchanges')"
          v-text="`${newExchangesCount} new exchanges`"
        />
      </tr>
    </tbody>
    <transition-group tag="tbody" name="fade-in">
      <template v-for="exchange in exchanges" :key="exchange.key">
        <tr
          class="is-selectable"
          :class="{
            'httpexchanges__exchange---is-detailed': showDetails[exchange.key],
          }"
          @click="
            showDetails[exchange.key]
              ? delete showDetails[exchange.key]
              : (showDetails[exchange.key] = true)
          "
        >
          <td
            class="httpexchanges__exchange-timestamp"
            v-text="exchange.timestamp.format('L HH:mm:ss.SSS')"
          />
          <td
            class="httpexchanges__exchange-method"
            v-text="exchange.request.method"
          />
          <td
            class="httpexchanges__exchange-uri"
            v-text="exchange.request.uri"
          />
          <td class="httpexchanges__exchange-status">
            <span
              class="tag"
              :class="{
                'is-muted': exchange.isPending(),
                'is-success': exchange.isSuccess(),
                'is-warning': exchange.isClientError(),
                'is-danger': exchange.isServerError(),
              }"
              v-text="exchange.response ? exchange.response.status : 'pending'"
            />
          </td>
          <td
            class="httpexchanges__exchange-contentType"
            v-text="exchange.contentTypeRequest"
          />
          <td
            class="httpexchanges__exchange-contentLength"
            v-text="
              exchange.contentLengthRequest
                ? prettyBytes(exchange.contentLengthRequest)
                : ''
            "
          />
          <td
            class="httpexchanges__exchange-contentType"
            v-text="exchange.contentTypeResponse"
          />
          <td
            class="httpexchanges__exchange-contentLength"
            v-text="
              exchange.contentLengthResponse
                ? prettyBytes(exchange.contentLengthResponse)
                : ''
            "
          />
          <td
            class="httpexchanges__exchange-timeTaken"
            v-text="
              exchange.timeTaken !== null &&
              typeof exchange.timeTaken !== 'undefined'
                ? `${toMilliseconds(parse(exchange.timeTaken)).toFixed(0)} ms`
                : ''
            "
          />
        </tr>
        <tr v-if="showDetails[exchange.key]" :key="`${exchange.key}-detail`">
          <td colspan="7">
            <pre
              class="httpexchanges__exchange-detail"
              v-text="toJson(exchange)"
            />
          </td>
        </tr>
      </template>
      <tr v-if="exchanges.length === 0" key="no-exchanges">
        <td
          class="is-muted"
          colspan="7"
          v-text="$t('instances.httpexchanges.no_exchanges_found')"
        />
      </tr>
    </transition-group>
  </table>
</template>

<script>
import { parse } from 'iso8601-duration';
import prettyBytes from 'pretty-bytes';

import { toMilliseconds } from '@/utils/iso8601-duration';

export default {
  props: {
    newExchangesCount: {
      type: Number,
      default: 0,
    },
    exchanges: {
      type: Array,
      default: () => [],
    },
  },
  emits: ['show-new-exchanges'],
  data: () => ({
    showDetails: {},
  }),
  methods: {
    prettyBytes,
    toJson(obj) {
      return JSON.stringify(obj, null, 4);
    },
    toMilliseconds,
    parse,
  },
};
</script>

<style lang="css">
.httpexchanges {
  table-layout: fixed;
}
.httpexchanges td {
  vertical-align: middle;
  overflow: hidden;
  word-wrap: break-word;
}
.httpexchanges__exchange--is-detailed td {
  border: none !important;
}
.httpexchanges__exchange-timestamp {
  width: 130px;
}
.httpexchanges__exchange-method {
  @apply font-mono;
  width: 100px;
}
.httpexchanges__exchange-uri {
  width: auto;
}
.httpexchanges__exchange-status {
  @apply font-mono;
  width: 80px;
}
.httpexchanges__exchange-contentType {
  width: 200px;
}
.httpexchanges__exchange-contentLength {
  width: 100px;
}
.httpexchanges__exchange-timeTaken {
  width: 120px;
}
.httpexchanges__exchange-detail {
  overflow: auto;
}
</style>
