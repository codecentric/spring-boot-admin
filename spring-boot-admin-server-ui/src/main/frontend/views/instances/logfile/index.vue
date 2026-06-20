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
            <span
              v-if="isRetryingLogfile"
              aria-live="polite"
              class="logfile-reconnecting"
              v-text="$t('instances.logfile.reconnecting')"
            />
            <small class="hidden md:block">
              <span v-text="skippedBytesString" />
              &nbsp;
              <span v-text="remainingBytesString" />
            </small>
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
              :aria-label="
                $t(
                  isFollowing
                    ? 'instances.logfile.stop_follow'
                    : 'instances.logfile.resume_follow',
                )
              "
              :disabled="isChunkLoading || isRetryingLogfile"
              :primary="isFollowing"
              :title="
                $t(
                  isFollowing
                    ? 'instances.logfile.stop_follow'
                    : 'instances.logfile.resume_follow',
                )
              "
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

          <sba-button
            class="hidden md:block"
            :disabled="isRetryingLogfile"
            @click="downloadLogfile()"
          >
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
      <table class="table-striped min-w-full">
        <tbody>
          <tr
            v-for="line in displayLines"
            :key="lineKey(line)"
            :class="{
              'log-viewer-highlighted-line':
                lineKey(line) === highlightedLineKey,
            }"
            :data-line-key="lineKey(line)"
          >
            <td>
              <br v-if="line.text === ''" />
              <pre v-else v-html="renderLine(line.text)" />
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
import { useNotificationCenter } from '@stekoe/vue-toast-notificationcenter';
import { AnsiUp } from 'ansi_up/ansi_up';
import prettyBytes from 'pretty-bytes';
import { debounceTime, fromEvent } from 'rxjs';

import subscribing from '@/mixins/subscribing';
import sbaConfig from '@/sba-config';
import Instance from '@/services/instance';
import autolink from '@/utils/autolink';
import {
  ChunkDirection,
  DEFAULT_LOGFILE_CHUNK_SIZE,
  StreamType,
  fetchLogfileRange,
} from '@/utils/logtail';
import {
  EMPTY,
  animationFrameScheduler,
  catchError,
  concatAll,
  concatMap,
  filter,
  from,
  map,
  of,
  retry,
  tap,
  timer,
} from '@/utils/rxjs';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SbaInstanceSection from '@/views/instances/shell/sba-instance-section';

const LogfileMode = Object.freeze({
  FOLLOW: 'follow',
  MANUAL: 'manual',
});

const SCROLL_BOTTOM_TOLERANCE = 2;
const MAX_BYTES_RENDERED = 600 * 1024;
const TAIL_PIN_MAINTENANCE_DURATION = 200;
const LOGFILE_RECONNECT_RESET_RETRY_COUNT = 10;

const notificationCenter = useNotificationCenter({});

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
    isTailPinned: true,
    renderedLines: [],
    displayLines: [],
    wrapLines: false,
    logTailSubscription: null,
    scrollSubscription: null,
    tailPinMaintenanceTimer: null,
    highlightedLineKey: null,
    highlightedLineTimer: null,
    isMaintainingTailPin: false,
    manualMetadataSubscription: null,
    manualChunkCache: {},
    mode: LogfileMode.FOLLOW,
    chunkSize: DEFAULT_LOGFILE_CHUNK_SIZE,
    windowStart: -1,
    windowEnd: -1,
    totalBytes: 0,
    loadedBytes: 0,
    isChunkLoading: false,
    isRetryingLogfile: false,
    shouldResetLogfileOnReconnect: false,
    faArrowDown,
    faArrowsDownToLine,
    faArrowUp,
  }),
  computed: {
    skippedBytes() {
      return Math.max(this.windowStart, 0);
    },
    skippedBytesString() {
      if (this.skippedBytes != null) {
        return `skipped ${prettyBytes(this.skippedBytes)}`;
      }
      return '';
    },
    remainingBytesString() {
      if (!this.isFollowing && this.totalBytes > 0) {
        return `remaining ${prettyBytes(this.totalBytes - this.windowEnd - 1)}`;
      }
      return '';
    },
    isFollowing() {
      return this.mode === LogfileMode.FOLLOW;
    },
    canLoadPrevious() {
      return this.windowStart > 0;
    },
    canLoadNext() {
      return (
        this.windowEnd >= 0 &&
        this.totalBytes > 0 &&
        this.windowEnd < this.totalBytes - 1
      );
    },
    canPageUp() {
      //Follow mode
      let canPageUpFollowMode =
        this.isFollowing &&
        (!this.atTop || (this.atTop && this.canLoadPrevious));
      //Manual mode
      let canPageUpManualMode =
        !this.isFollowing && (!this.atTop || this.canLoadPrevious);

      return (
        !this.isChunkLoading &&
        !this.isRetryingLogfile &&
        (canPageUpFollowMode || canPageUpManualMode)
      );
    },
    canPageDown() {
      //Follow mode
      let canPageDownFollowMode = this.isFollowing && !this.atBottom;
      //Manual mode
      let canPageDownManualMode =
        !this.isFollowing && (!this.atBottom || this.canLoadNext);

      return (
        !this.isChunkLoading &&
        !this.isRetryingLogfile &&
        (canPageDownFollowMode || canPageDownManualMode)
      );
    },
  },
  created() {
    this.ansiUp = new AnsiUp();
    this.textEncoder = new TextEncoder();
  },
  mounted() {
    const element = this.$refs.scrollContainer;
    this.scrollSubscription = fromEvent(element, 'scroll')
      .pipe(debounceTime(25))
      .subscribe(() => this.updateScrollState());

    if (this.isFollowing && this.displayLines.length > 0) {
      this.scrollToBottom();
    }
  },
  beforeUnmount() {
    this.stopManualMetadataPolling();
    this.unsubscribe();
    this.stopScrollSubscription();
    this.stopTailPinMaintenance();
    this.stopLineHighlight();
  },
  methods: {
    prettyBytes,
    byteLength(content) {
      return this.textEncoder.encode(content).length;
    },
    lineKey(line) {
      return `${line.startByte}-${line.endByte}`;
    },
    createLine(text, startByte, endByte) {
      return { text, startByte, endByte };
    },
    splitLines(content, windowStart) {
      if (content === '') {
        return [];
      }
      let nextByte = windowStart;
      const lines = content.split(/\n/);
      return lines
      .filter((line, index) => index < lines.length - 1)
      .map((line, index) => {
        const lineBytes = this.byteLength(line) + this.byteLength('\n');
        const renderedLine = this.createLine(
          line,
          nextByte,
          nextByte + lineBytes - 1,
        );
        nextByte += lineBytes;
        return renderedLine;
      });
    },
    calculateLoadedBytes() {
      return Math.max(this.windowEnd - this.windowStart + 1, 0);
    },
    getLineByteLength(line) {
      // +2 is for \n
      return Math.max(line.endByte - line.startByte + 2, 0);
    },
    appendRenderedLines(lines, windowStart, windowEnd) {
      let linesToAppend = [...lines];
      this.renderedLines = [...this.renderedLines, ...linesToAppend];
      if(this.windowStart == -1){
        //first append
        this.windowStart = windowStart;
      }else{
        this.windowStart = Math.min(windowStart, this.windowStart);
      }
      this.windowEnd = windowEnd;
      if (this.calculateLoadedBytes() > MAX_BYTES_RENDERED) {
        this.evictOverflowingRenderedLines();
      }
      this.displayLines = this.renderedLines;
    },
    evictOverflowingRenderedLines() {
      let evictedBytes = 0;
      let evictedLines = 0;
      let previousSize = this.calculateLoadedBytes();
      while (
        previousSize - evictedBytes > DEFAULT_LOGFILE_CHUNK_SIZE &&
        evictedLines < this.renderedLines.length
      ) {
        evictedBytes += this.getLineByteLength(
          this.renderedLines[evictedLines],
        );
        evictedLines += 1;
      }
      this.renderedLines.splice(0, evictedLines);
      this.windowStart =
        this.renderedLines[0]?.startByte ?? this.windowEnd + 1;
    },
    renderLine(line) {
      return autolink(this.ansiUp.ansi_to_html(line));
    },
    resetLogfileWindowState() {
      this.renderedLines = [];
      this.displayLines = [];
      this.loadedBytes = 0;
      this.windowStart = -1;
      this.windowEnd = -1;
      this.totalBytes = 0;
      this.stopLineHighlight();
    },
    resetFollowState() {
      this.stopManualMetadataPolling();
      this.unsubscribe();
      this.mode = LogfileMode.FOLLOW;
      this.error = null;
      this.hasLoaded = false;
      this.isChunkLoading = false;
      this.isRetryingLogfile = false;
      this.shouldResetLogfileOnReconnect = false;
      this.atBottom = false;
      this.atTop = true;
      this.isTailPinned = true;
      this.resetLogfileWindowState();
    },
    isLogfileRangeInvalid(error) {
      return error?.response?.status === 416;
    },
    notifyLogfileCompressed() {
      notificationCenter.warning(this.$t('instances.logfile.compressed_reset'));
    },
    async handleLogfileCompressed() {
      this.notifyLogfileCompressed();
      this.resetFollowState();
      await this.subscribe();
    },
    startManualMetadataPolling() {
      this.manualMetadataSubscription = timer(
        0,
        sbaConfig.uiSettings.pollTimer.logfile,
      )
        .pipe(
          concatMap(() =>
            from(fetchLogfileRange(this.instance, 0, 0)).pipe(
              tap((response) => {
                if (!this.isFollowing) {
                  if (response.totalBytes <= this.windowEnd) {
                    void this.handleLogfileCompressed();
                    return;
                  }
                  this.totalBytes = response.totalBytes;
                  this.error = null;
                  this.isRetryingLogfile = false;
                }
              }),
              catchError((error) => {
                if (this.isLogfileRangeInvalid(error)) {
                  void this.handleLogfileCompressed();
                  return EMPTY;
                }
                console.warn('Fetching logfile metadata failed:', error);
                this.hasLoaded = true;
                this.error = error;
                this.isRetryingLogfile = true;
                return EMPTY;
              }),
            ),
          ),
        )
        .subscribe();
    },
    stopManualMetadataPolling() {
      try {
        this.manualMetadataSubscription?.unsubscribe();
      } finally {
        this.manualMetadataSubscription = null;
      }
    },
    unsubscribe() {
      try {
        this.logTailSubscription?.unsubscribe();
      } finally {
        this.logTailSubscription = null;
      }
    },
    async subscribe() {
      if (this.logTailSubscription && !this.logTailSubscription.closed) {
        return;
      }

      this.unsubscribe();
      this.logTailSubscription = await this.createSubscription();
    },
    stopScrollSubscription() {
      try {
        this.scrollSubscription?.unsubscribe();
      } finally {
        this.scrollSubscription = null;
      }
    },
    maintainTailPin() {
      if (!this.isFollowing || !this.isTailPinned) {
        return;
      }

      this.isMaintainingTailPin = true;
      window.clearTimeout(this.tailPinMaintenanceTimer);
      this.tailPinMaintenanceTimer = window.setTimeout(() => {
        this.tailPinMaintenanceTimer = null;
        this.isMaintainingTailPin = false;
        this.updateScrollState();
      }, TAIL_PIN_MAINTENANCE_DURATION);
    },
    stopTailPinMaintenance() {
      window.clearTimeout(this.tailPinMaintenanceTimer);
      this.tailPinMaintenanceTimer = null;
      this.isMaintainingTailPin = false;
    },
    getMaxScrollTop(element) {
      return Math.max(element.scrollHeight - element.clientHeight, 0);
    },
    isScrolledToBottom(element) {
      return (
        this.getMaxScrollTop(element) - element.scrollTop <=
        SCROLL_BOTTOM_TOLERANCE
      );
    },
    syncScrollState(element = this.$refs.scrollContainer) {
      if (!element) {
        return;
      }

      this.atTop = element.scrollTop <= SCROLL_BOTTOM_TOLERANCE;
      this.atBottom = this.isScrolledToBottom(element);

      if (this.isFollowing) {
        if (this.atBottom) {
          this.isTailPinned = true;
        } else if (!this.isMaintainingTailPin) {
          this.isTailPinned = false;
        }
      }
    },
    async setManualChunk(response, direction, scrollAnchorByte = null) {
      let {data, totalBytes, windowStart, windowEnd, status} = response;

      let renderedLines = this.splitLines(data, windowStart);
      this.windowStart = windowStart;
      this.windowEnd = windowEnd;
      this.totalBytes = totalBytes;
      this.displayLines = renderedLines;
      
      this.hasLoaded = true;
      await this.$nextTick();
      if (scrollAnchorByte != null) {
        if (this.scrollToLineContainingByte(scrollAnchorByte, true)) {
          return;
        }
        this.scrollToBottom();
        return;
      }
      this.scrollToTop();
    },
    createSubscription() {
      this.unsubscribe();
      return this.instance
        .streamLogfile(sbaConfig.uiSettings.pollTimer.logfile)
        .pipe(
          tap((part) => {
            if (part.type === StreamType.Reset) {
              void this.handleLogfileCompressed();
              return;
            }
            if (part.type === StreamType.Empty) {
              console.warn('File size is 0 bytes');
              if (this.shouldResetLogfileOnReconnect) {
                this.resetLogfileWindowState();
              }
              this.hasLoaded = true;
              this.error = null;
              this.isRetryingLogfile = false;
              this.shouldResetLogfileOnReconnect = false;
            }
          }),
          filter((part) => part.type === StreamType.Data),
          map((part) =>
            of(
              {
                lines: this.splitLines(part.addendum, part.windowStart),
                windowStart: part.windowStart,
                windowEnd: part.windowEnd,
                totalBytes: part.totalBytes,
              },
              animationFrameScheduler,
            ),
          ),
          concatAll(),
          retry({
            count: Infinity,
            delay: (error, retryCount) => {
              this.hasLoaded = true;
              console.warn('Fetching logfile failed:', error);
              this.error = error;
              this.isRetryingLogfile = true;
              this.shouldResetLogfileOnReconnect =
                this.shouldResetLogfileOnReconnect ||
                retryCount > LOGFILE_RECONNECT_RESET_RETRY_COUNT;
              return timer(sbaConfig.uiSettings.pollTimer.logfile);
            },
          }),
        )
        .subscribe({
          next: ({ lines, windowStart, windowEnd, totalBytes }) => {
            const shouldKeepAtBottom = this.isFollowing && this.isTailPinned;
            if (shouldKeepAtBottom) {
              this.maintainTailPin();
            }
            if (this.shouldResetLogfileOnReconnect) {
              this.resetLogfileWindowState();
            }
            this.totalBytes = totalBytes;
            this.appendRenderedLines(lines, windowStart, windowEnd);
            this.hasLoaded = true;
            this.error = null;
            this.isRetryingLogfile = false;
            this.shouldResetLogfileOnReconnect = false;
            if (shouldKeepAtBottom) {
              this.$nextTick(() => {
                if (this.isFollowing) {
                  this.scrollToBottom();
                }
              });
            }
          },
          error: (error) => {
            this.hasLoaded = true;
            console.warn('Fetching logfile failed:', error);
            this.error = error;
            this.isRetryingLogfile = false;
            this.shouldResetLogfileOnReconnect = false;
            this.logTailSubscription = null;
          },
        });
    },
    async loadChunk(start, end, direction, scrollAnchorByte = null) {
      this.isChunkLoading = true;
      try {
        const response = await fetchLogfileRange(this.instance, start, end, direction);
        await this.setManualChunk(response, direction, scrollAnchorByte);
      } catch (error) {
        if (this.isLogfileRangeInvalid(error)) {
          await this.handleLogfileCompressed();
          return;
        }
        console.warn('Fetching logfile chunk failed:', error);
        this.error = error;
      } finally {
        this.isChunkLoading = false;
      }
    },
    async loadPreviousChunk() {
       const currentWindowStart =
        this.displayLines[0]?.startByte ?? this.windowStart;
      let end = currentWindowStart - 1;
      const start = Math.max(0, end - this.chunkSize + 1);
      if (start === 0) {
        end = Math.min(this.totalBytes - 1, this.chunkSize - 1);
      }
      const direction =
        end < currentWindowStart
          ? ChunkDirection.PREVIOUS
          : ChunkDirection.REPLACE;
      await this.loadChunk(start, end, direction);
    },
    async loadNextChunk() {
      const currentWindowEnd =
        this.displayLines.at(-1)?.endByte ?? this.windowEnd;
      const firstNewByte = currentWindowEnd + 1;
      let start = firstNewByte;
      const end = Math.min(this.totalBytes - 1, start + this.chunkSize - 1);
      if (end === this.totalBytes - 1) {
        start = Math.max(0, end - this.chunkSize + 1);
      }
      const direction =
        start > currentWindowEnd ? ChunkDirection.NEXT : ChunkDirection.REPLACE;
      const scrollAnchorByte = start <= currentWindowEnd ? firstNewByte : null;
      await this.loadChunk(start, end, direction, scrollAnchorByte);
    },
    async toggleFollowMode() {
      if (this.isFollowing) {
        this.unsubscribe();
        this.stopTailPinMaintenance();
        this.mode = LogfileMode.MANUAL;
        this.isTailPinned = false;
        this.startManualMetadataPolling();
      } else {
        this.resetFollowState();
        this.maintainTailPin();
        await this.subscribe();
        this.$nextTick(() => this.scrollToBottom());
      }
    },
    async pageUp() {
      //Follow Mode
      if (this.isFollowing) {
        if (this.atTop) {
          //switch to manual mode Toggle and also move previous chunk pending
          await this.toggleFollowMode();
          await this.loadPreviousChunk();
        } else {
          this.stopTailPinMaintenance();
          this.isTailPinned = false;
          this.scrollToTop();
        }
      }
      //Manual Mode
      else {
        this.syncScrollState();
        if (!this.atTop) {
          this.scrollToTop();
        } else if (this.canLoadPrevious) {
          await this.loadPreviousChunk();
        }
      }
    },
    async pageDown() {
      this.syncScrollState();

      //Follow mode
      if (this.isFollowing) {
        if (!this.atBottom) {
          this.scrollToBottom();
        }
      }
      //Manual Mode
      else {
        if (!this.atBottom) {
          this.scrollToBottom();
        } else if (this.canLoadNext) {
          await this.loadNextChunk();
        }
      }
    },
    updateScrollState() {
      this.syncScrollState();
    },
    scrollToTop() {
      const element = this.$refs.scrollContainer;
      if (!element) {
        return;
      }
      element.scrollTop = 0;
      this.syncScrollState(element);
    },
    stopLineHighlight() {
      window.clearTimeout(this.highlightedLineTimer);
      this.highlightedLineTimer = null;
      this.highlightedLineKey = null;
    },
    highlightLine(line) {
      this.stopLineHighlight();
      this.highlightedLineKey = this.lineKey(line);
      this.highlightedLineTimer = window.setTimeout(() => {
        this.highlightedLineTimer = null;
        this.highlightedLineKey = null;
      }, 1400);
    },
    scrollToLineContainingByte(byteOffset, highlight = false) {
      const element = this.$refs.scrollContainer;
      if (!element) {
        return false;
      }

      const line = this.displayLines.find(
        (displayLine) => displayLine.endByte >= byteOffset,
      );
      if (!line) {
        return false;
      }

      const target = element.querySelector(
        `[data-line-key="${this.lineKey(line)}"]`,
      );
      if (!target) {
        return false;
      }

      const scrollOffset =
        target.getBoundingClientRect().top -
        element.getBoundingClientRect().top;
      element.scrollTop += scrollOffset;
      this.syncScrollState(element);
      if (highlight) {
        this.highlightLine(line);
      }
      return true;
    },
    scrollToBottom() {
      const element = this.$refs.scrollContainer;
      if (!element) {
        return;
      }
      element.scrollTop = this.getMaxScrollTop(element);
      this.syncScrollState(element);

      if (this.isFollowing) {
        this.isTailPinned = true;
      }

      this.$nextTick(() => {
        const element = this.$refs.scrollContainer;
        if (!element) {
          return;
        }
        element.scrollTop = this.getMaxScrollTop(element);
        this.syncScrollState(element);
      });
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

.logfile-reconnecting {
  @apply text-sm font-semibold text-red-600 whitespace-nowrap;

  animation: logfile-retry-flicker 1s ease-in-out infinite;
}

@keyframes logfile-retry-flicker {
  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.35;
  }
}

.log-viewer {
  overflow: auto;
  margin: 0;
  padding: 0;
  width: 100%;
  max-height: 100%;
}

.log-viewer tr,
.log-viewer td,
.log-viewer br {
  @apply w-full;
}

.log-viewer pre {
  padding: 0 0.75em;
  margin-bottom: 1px;
  @apply w-full;
}

.log-viewer td:hover {
  background: #dbdbdb;
}

.log-viewer-highlighted-line pre {
  animation: logfile-line-highlight 1.4s ease-out;
}

@keyframes logfile-line-highlight {
  0% {
    background: #fef3c7;
  }

  100% {
    background: transparent;
  }
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
