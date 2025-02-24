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
  <sba-instance-section :error="error" :loading="!hasLoaded">
    <template #before>
      <sba-sticky-subnav>
        <div class="flex items-center justify-end gap-1">
          <div class="flex-1">
            <span v-text="$t('instances.logfile.label')" />&nbsp;
            <small class="hidden md:block" v-text="skippedBytesString" />
          </div>
          <div class="flex items-start">
            <div class="flex items-center h-5">
              <input
                id="wraplines"
                v-model="wrapLines"
                class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded"
                name="wraplines"
                type="checkbox"
              />
            </div>
            <div class="ml-3 text-sm">
              <label
                class="font-medium text-gray-700"
                for="wraplines"
                v-text="$t('instances.logfile.wrap_lines')"
              />
            </div>
          </div>

          <div class="mx-3 btn-group">
            <sba-button :disabled="atTop" @click="scrollToTop">
              <svg
                class="h-4 w-4"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M7 11l5-5m0 0l5 5m-5-5v12"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </sba-button>
            <sba-button :disabled="atBottom" @click="scrollToBottom">
              <svg
                class="h-4 w-4"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M17 13l-5 5m0 0l-5-5m5 5V6"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </sba-button>
          </div>

          <sba-button class="hidden md:block" @click="downloadLogfile()">
            <font-awesome-icon icon="download" />&nbsp;
            <span v-text="$t('instances.logfile.download')" />
          </sba-button>
        </div>
      </sba-sticky-subnav>
    </template>

    <div
      :class="{ 'wrap-lines': wrapLines }"
      class="log-viewer overflow-x-auto text-sm -mx-6 -my-20 pt-14"
    >
      <table class="table-striped" />
    </div>
  </sba-instance-section>
</template>

<script>
import { AnsiUp } from 'ansi_up/ansi_up';
import { chunk } from 'lodash-es';
import prettyBytes from 'pretty-bytes';
import { debounceTime, fromEvent } from 'rxjs';

import subscribing from '@/mixins/subscribing';
import sbaConfig from '@/sba-config';
import Instance from '@/services/instance';
import autolink from '@/utils/autolink';
import {
  animationFrameScheduler,
  concatAll,
  concatMap,
  map,
  of,
  tap,
} from '@/utils/rxjs';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

export default {
  components: { SbaInstanceSection },
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    atBottom: false,
    atTop: true,
    skippedBytes: null,
    wrapLines: true,
    scrollSubcription: null,
  }),
  computed: {
    skippedBytesString() {
      if (this.skippedBytes != null) {
        return `skipped ${prettyBytes(this.skippedBytes)}`;
      }
      return '';
    },
  },
  created() {
    this.ansiUp = new AnsiUp();
    this.scrollSubcription = fromEvent(window, 'scroll')
      .pipe(
        debounceTime(25),
        map((event) => event.target.scrollingElement.scrollTop),
      )
      .subscribe((scrollTop) => {
        this.atTop = scrollTop === 0;
        this.atBottom =
          document.scrollingElement.clientHeight ===
          document.scrollingElement.scrollHeight -
            document.scrollingElement.scrollTop;
      });
  },
  beforeUnmount() {
    if (this.scrollSubcription && !this.scrollSubcription.closed) {
      try {
        this.scrollSubcription.unsubscribe();
      } finally {
        this.scrollSubcription = null;
      }
    }
  },
  methods: {
    prettyBytes,
    createSubscription() {
      this.error = null;
      return this.instance
        .streamLogfile(sbaConfig.uiSettings.pollTimer.logfile)
        .pipe(
          tap(
            (part) => (this.skippedBytes = this.skippedBytes || part.skipped),
          ),
          concatMap((part) => chunk(part.addendum.split(/\r?\n/), 250)),
          map((lines) => of(lines, animationFrameScheduler)),
          concatAll(),
        )
        .subscribe({
          next: (lines) => {
            this.hasLoaded = true;
            lines.forEach((line) => {
              const row = document.createElement('tr');
              const col = document.createElement('td');
              const pre = document.createElement('pre');
              pre.innerHTML = autolink(this.ansiUp.ansi_to_html(line));
              col.appendChild(pre);
              row.appendChild(col);
              document.querySelector('.log-viewer > table')?.appendChild(row);
            });

            if (this.atBottom) {
              this.scrollToBottom();
            }
          },
          error: (error) => {
            this.hasLoaded = true;
            console.warn('Fetching logfile failed:', error);
            this.error = error;
          },
        });
    },
    scrollToTop() {
      document.scrollingElement.scrollTop = 0;
    },
    scrollToBottom() {
      document.scrollingElement.scrollTop =
        document.scrollingElement.scrollHeight;
    },
    downloadLogfile() {
      window.open(`instances/${this.instance.id}/actuator/logfile`, '_blank');
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/logfile',
      parent: 'instances',
      path: 'logfile',
      component: this,
      label: 'instances.logfile.label',
      group: VIEW_GROUP.LOGGING,
      order: 200,
      isEnabled: ({ instance }) => instance.hasEndpoint('logfile'),
    });
  },
};
</script>

<style lang="css">
.log-viewer pre {
  padding: 0 0.75em;
  margin-bottom: 1px;
}

.log-viewer pre:hover {
  background: #dbdbdb;
}

.log-viewer.wrap-lines pre {
  @apply whitespace-pre-wrap;
}

.log-viewer {
  background-color: #fff;
  overflow: auto;
}
</style>
