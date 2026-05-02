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
  <sba-instance-section
    :error="error"
    :loading="!hasLoaded"
    :layout-options="{ isFlex: true, noMargin: true }"
    class="logfile-section"
  >
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
            <sba-button
              :aria-label="$t('instances.logfile.resume_follow')"
              :disabled="isChunkLoading"
              :primary="isFollowing"
              :title="$t('instances.logfile.resume_follow')"
              @click="toggleFollowMode"
            >
              <font-awesome-icon :icon="faArrowsDownToLine" />
            </sba-button>
            <sba-button
              :aria-label="
                $t(
                  isFollowing
                    ? 'instances.logfile.page_up'
                    : 'instances.logfile.previous_chunk',
                )
              "
              :disabled="!canPageUp"
              :title="
                $t(
                  isFollowing
                    ? 'instances.logfile.page_up'
                    : 'instances.logfile.previous_chunk',
                )
              "
              @click="pageUp"
            >
              <font-awesome-icon :icon="faArrowUp" />
            </sba-button>
            <sba-button
              :aria-label="
                $t(
                  isFollowing
                    ? 'instances.logfile.page_down'
                    : 'instances.logfile.next_chunk',
                )
              "
              :disabled="!canPageDown"
              :title="
                $t(
                  isFollowing
                    ? 'instances.logfile.page_down'
                    : 'instances.logfile.next_chunk',
                )
              "
              @click="pageDown"
            >
              <font-awesome-icon :icon="faArrowDown" />
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
      ref="scrollContainer"
      :class="{ 'wrap-lines': wrapLines }"
      class="log-viewer overflow-x-auto text-sm -mx-6 -my-20 pt-14"
    >
      <table class="table-striped">
        <tbody>
          <tr
            v-for="(line, index) in renderedLines"
            :key="`${windowStart}-${index}`"
          >
            <td>
              <pre v-html="renderLine(line)" />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </sba-instance-section>
</template>

<script>
import {
  faArrowDown,
  faArrowUp,
  faArrowsDownToLine,
} from '@fortawesome/free-solid-svg-icons';
import { AnsiUp } from 'ansi_up/ansi_up';
import { chunk } from 'lodash-es';
import prettyBytes from 'pretty-bytes';
import { debounceTime, fromEvent } from 'rxjs';

import subscribing from '@/mixins/subscribing';
import sbaConfig from '@/sba-config';
import Instance from '@/services/instance';
import autolink from '@/utils/autolink';
import { DEFAULT_LOGFILE_CHUNK_SIZE } from '@/utils/logtail';
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
    renderedLines: [],
    wrapLines: false,
    scrollSubscription: null,
    mode: 'follow',
    chunkSize: DEFAULT_LOGFILE_CHUNK_SIZE,
    windowStart: 0,
    windowEnd: -1,
    totalBytes: 0,
    isChunkLoading: false,
    faArrowDown,
    faArrowsDownToLine,
    faArrowUp,
  }),
  computed: {
    skippedBytesString() {
      if (this.skippedBytes != null && this.skippedBytes > 0) {
        return `skipped ${prettyBytes(this.skippedBytes)}`;
      }
      return '';
    },
    isFollowing() {
      return this.mode === 'follow';
    },
    canLoadPrevious() {
      return !this.isChunkLoading && this.windowStart > 0;
    },
    canLoadNext() {
      return (
        !this.isFollowing &&
        !this.isChunkLoading &&
        this.windowEnd >= 0 &&
        this.totalBytes > 0 &&
        this.windowEnd < this.totalBytes - 1
      );
    },
    canPageUp() {
      return !this.isChunkLoading && (!this.atTop || this.canLoadPrevious);
    },
    canPageDown() {
      return !this.isChunkLoading && !(this.isFollowing && this.atBottom);
    },
  },
  created() {
    this.ansiUp = new AnsiUp();
  },
  mounted() {
    const element = this.$refs.scrollContainer;
    this.scrollSubscription = fromEvent(element, 'scroll')
      .pipe(debounceTime(25))
      .subscribe(() => this.updateScrollState());

    if (this.isFollowing && this.renderedLines.length > 0) {
      this.scrollToBottom();
    }
  },
  beforeUnmount() {
    if (this.scrollSubscription && !this.scrollSubscription.closed) {
      try {
        this.scrollSubscription.unsubscribe();
      } finally {
        this.scrollSubscription = null;
      }
    }
  },
  methods: {
    prettyBytes,
    splitLines(content) {
      return content === '' ? [] : content.split(/\r?\n/);
    },
    renderLine(line) {
      return autolink(this.ansiUp.ansi_to_html(line));
    },
    resetFollowState() {
      this.mode = 'follow';
      this.error = null;
      this.hasLoaded = false;
      this.isChunkLoading = false;
      this.skippedBytes = null;
      this.renderedLines = [];
      this.windowStart = 0;
      this.windowEnd = -1;
      this.totalBytes = 0;
    },
    async setManualChunk(response) {
      this.renderedLines = this.splitLines(response.data);
      this.windowStart = response.windowStart;
      this.windowEnd = response.windowEnd;
      this.totalBytes = response.totalBytes;
      this.skippedBytes = response.windowStart;
      this.hasLoaded = true;
      await this.$nextTick();
      this.scrollToTop();
    },
    createSubscription() {
      this.resetFollowState();
      let shouldScrollToBottom = true;

      return this.instance
        .streamLogfile(sbaConfig.uiSettings.pollTimer.logfile)
        .pipe(
          tap((part) => {
            if (this.renderedLines.length === 0) {
              this.windowStart = part.windowStart;
              this.skippedBytes = part.skipped || null;
            }
            this.windowEnd = part.windowEnd;
            this.totalBytes = part.totalBytes;
          }),
          concatMap((part) => chunk(part.addendum.split(/\r?\n/), 250)),
          map((lines) => of(lines, animationFrameScheduler)),
          concatAll(),
        )
        .subscribe({
          next: (lines) => {
            this.hasLoaded = true;
            this.renderedLines = [...this.renderedLines, ...lines];

            const shouldKeepAtBottom = shouldScrollToBottom || this.atBottom;
            shouldScrollToBottom = false;

            if (shouldKeepAtBottom) {
              this.$nextTick(() => this.scrollToBottom());
            }
          },
          error: (error) => {
            this.hasLoaded = true;
            console.warn('Fetching logfile failed:', error);
            this.error = error;
          },
        });
    },
    async loadChunk(start, end) {
      this.error = null;
      this.mode = 'manual';
      this.isChunkLoading = true;

      try {
        const response = await this.instance.fetchLogfileRange(start, end);
        await this.setManualChunk(response);
      } catch (error) {
        this.hasLoaded = true;
        console.warn('Fetching logfile chunk failed:', error);
        this.error = error;
      } finally {
        this.isChunkLoading = false;
      }
    },
    async loadPreviousChunk() {
      if (!this.canLoadPrevious) {
        return;
      }

      this.unsubscribe();
      const end = this.windowStart - 1;
      const start = Math.max(0, end - this.chunkSize + 1);
      await this.loadChunk(start, end);
    },
    async loadNextChunk() {
      if (!this.canLoadNext) {
        return;
      }

      this.unsubscribe();
      const start = this.windowEnd + 1;
      const end = Math.min(this.totalBytes - 1, start + this.chunkSize - 1);
      await this.loadChunk(start, end);
    },
    async resumeFollowMode() {
      if (this.isFollowing && this.subscription) {
        return;
      }

      this.unsubscribe();
      await this.subscribe();
    },
    async toggleFollowMode() {
      if (this.isFollowing) {
        this.unsubscribe();
        this.mode = 'manual';
        return;
      }

      await this.resumeFollowMode();
    },
    async pageUp() {
      if (this.atTop) {
        await this.loadPreviousChunk();
        return;
      }

      this.scrollToTop();
    },
    async pageDown() {
      if (!this.atBottom) {
        this.scrollToBottom();
        return;
      }

      if (this.canLoadNext) {
        await this.loadNextChunk();
        if (!this.canLoadNext) {
          await this.resumeFollowMode();
        }
        return;
      }

      if (!this.isFollowing) {
        await this.resumeFollowMode();
      }
    },
    updateScrollState() {
      const element = this.$refs.scrollContainer;
      if (!element) {
        return;
      }

      this.atTop = element.scrollTop === 0;
      this.atBottom =
        Math.abs(
          element.clientHeight - (element.scrollHeight - element.scrollTop),
        ) <= 1;
    },
    scrollToTop() {
      this.$refs.scrollContainer.scrollTop = 0;
      this.updateScrollState();
    },
    scrollToBottom() {
      this.$refs.scrollContainer.scrollTop =
        this.$refs.scrollContainer.scrollHeight;
      this.updateScrollState();
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
@reference "../../../index.css";
.logfile-section {
  display: flex;
  flex-direction: column;
  max-height: 100%;
}

.logfile-section #subnavigation {
  flex: 0 0 auto;
}

.logfile-section .sba-instance-section-content {
  min-height: 0;
  padding: 0;
  overflow: hidden;
  flex-direction: column;
}

.logfile-section .sba-instance-section-content [role='alert'] {
  margin: 0;
}

.log-viewer {
  overflow: auto;
  margin: 0;
  padding: 0;
  width: 100%;
  max-height: 100%;
}

.log-viewer tr,
.log-viewer td {
  @apply w-full;
}

.log-viewer pre {
  padding: 0 0.75em;
  margin-bottom: 1px;
}

.log-viewer td:hover {
  background: #dbdbdb;
}

.log-viewer a[href] {
  @apply underline;
}

.log-viewer.wrap-lines pre {
  @apply whitespace-pre-wrap;
  word-break: break-all;
}

.log-viewer {
  background-color: #fff;
  overflow: auto;
}
</style>
