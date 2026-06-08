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
import { DEFAULT_LOGFILE_CHUNK_SIZE, StreamType } from '@/utils/logtail';
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
const ChunkDirection = Object.freeze({
  PREVIOUS: 'previous',
  NEXT: 'next',
  REPLACE: 'replace',
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
    tailRemainderState: {
      remainderString: '',
      presentOrnotBoolean: false,
      byteStart: -1,
      byteEnd: -1,
    },
    headRemainderState: {
      remainderString: '',
      presentOrnotBoolean: false,
      byteStart: -1,
      byteEnd: -1,
    },
    wrapLines: false,
    logTailSubscription: null,
    scrollSubscription: null,
    tailPinMaintenanceTimer: null,
    highlightedLineKey: null,
    highlightedLineTimer: null,
    isMaintainingTailPin: false,
    manualMetadataSubscription: null,
    mode: LogfileMode.FOLLOW,
    chunkSize: DEFAULT_LOGFILE_CHUNK_SIZE,
    windowStart: 0,
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
      return lines.map((line, index) => {
        const hasNewLine = index < lines.length - 1;
        const lineBytes =
          this.byteLength(line) + (hasNewLine ? this.byteLength('\n') : 0);
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
    clearRemainderStates() {
      this.clearHeadRemainderState();
      this.clearTailRemainderState();
    },
    clearTailRemainderState() {
      this.setTailRemainderState('', -1, -1);
      this.tailRemainderState.presentOrnotBoolean = false;
    },
    setTailRemainderState(remainderString, byteStart, byteEnd) {
      this.tailRemainderState.presentOrnotBoolean = true;
      this.tailRemainderState.remainderString = remainderString;
      this.tailRemainderState.byteStart = byteStart;
      this.tailRemainderState.byteEnd = byteEnd;
    },
    clearHeadRemainderState() {
      this.setHeadRemainderState('', -1, -1);
      this.headRemainderState.presentOrnotBoolean = false;
    },
    setHeadRemainderState(remainderString, byteStart, byteEnd) {
      this.headRemainderState.presentOrnotBoolean = true;
      this.headRemainderState.remainderString = remainderString;
      this.headRemainderState.byteStart = byteStart;
      this.headRemainderState.byteEnd = byteEnd;
    },
    getLineByteLength(line) {
      return Math.max(line.endByte - line.startByte + 1, 0);
    },
    setTailRemainderLine(line) {
      this.setTailRemainderState(line.text, line.startByte, line.endByte);
    },
    setHeadRemainderLine(line) {
      this.setHeadRemainderState(line.text, line.startByte, line.endByte);
    },
    appendToTailRemainder(line) {
      this.setTailRemainderState(
        this.tailRemainderState.remainderString + line.text,
        this.tailRemainderState.presentOrnotBoolean
          ? this.tailRemainderState.byteStart
          : line.startByte,
        line.endByte,
      );
    },
    mergeTailRemainderWithLine(line) {
      return this.createLine(
        this.tailRemainderState.remainderString + line.text,
        this.tailRemainderState.byteStart,
        line.endByte,
      );
    },
    mergeLineWithHeadRemainder(line) {
      return this.createLine(
        line.text + this.headRemainderState.remainderString,
        line.startByte,
        this.headRemainderState.byteEnd,
      );
    },
    appendRenderedLines(lines, windowStart, windowEnd) {
      let linesToAppend = [...lines];
      this.windowEnd = windowEnd;
      if (linesToAppend.length === 0) {
        return;
      }
      //first pass
      if (
        this.renderedLines.length === 0 &&
        !this.headRemainderState.presentOrnotBoolean &&
        !this.tailRemainderState.presentOrnotBoolean
      ) {
        if (linesToAppend.length === 1) {
          this.appendToTailRemainder(linesToAppend[0]);
          this.windowStart = windowStart;
          return;
        }
        this.renderedLines = linesToAppend;
        this.windowStart = windowStart;
        if (this.windowStart > 0) {
          const headLine = this.renderedLines[0];
          if (headLine && headLine.text !== '') {
            this.setHeadRemainderLine(headLine);
          }
          this.renderedLines.shift();
        }
        const tailLine = this.renderedLines.at(-1);
        if (tailLine && tailLine.text !== '') {
          this.setTailRemainderLine(tailLine);
        }
        if (this.renderedLines.length > 0) {
          this.renderedLines.pop();
        }
        this.displayLines = this.renderedLines;
      } else {
        //consequent passes
        if (linesToAppend.length === 1) {
          // If a few bytes are added without the newline , simply update remainders do not render anything new.
          this.appendToTailRemainder(linesToAppend[0]);
          return;
        }
        //no remainder
        if (!this.tailRemainderState.presentOrnotBoolean) {
          this.windowEnd = windowEnd;
          this.renderedLines = [...this.renderedLines, ...linesToAppend];
        }
        //remainder present
        else if (this.tailRemainderState.presentOrnotBoolean) {
          this.windowEnd = windowEnd;
          let completedLine = this.mergeTailRemainderWithLine(linesToAppend[0]);
          linesToAppend.shift();
          this.renderedLines = [
            ...this.renderedLines,
            completedLine,
            ...linesToAppend,
          ];
          this.clearTailRemainderState();
        }
        const tailLine = this.renderedLines.at(-1);
        if (tailLine && tailLine.text !== '') {
          this.setTailRemainderLine(tailLine);
        }
        if (this.renderedLines.length > 0) {
          this.renderedLines.pop();
        }
        if (this.calculateLoadedBytes() > MAX_BYTES_RENDERED) {
          this.evictOverflowingRenderedLines();
        }
        this.displayLines = this.renderedLines;
      }
    },
    evictOverflowingRenderedLines() {
      let evictedBytes = 0;
      let evictedLines = 0;
      let previousSize = this.calculateLoadedBytes();
      if (this.headRemainderState.presentOrnotBoolean) {
        evictedBytes += Math.max(
          this.headRemainderState.byteEnd -
            this.headRemainderState.byteStart +
            1,
          0,
        );
        this.clearHeadRemainderState();
      }
      while (
        previousSize - evictedBytes > DEFAULT_LOGFILE_CHUNK_SIZE &&
        evictedLines < this.renderedLines.length
      ) {
        evictedBytes += this.getLineByteLength(
          this.renderedLines[evictedLines],
        );
        evictedLines += 1;
      }
      this.windowStart += evictedBytes;
      this.renderedLines.splice(0, evictedLines);
    },
    renderLine(line) {
      return autolink(this.ansiUp.ansi_to_html(line));
    },
    resetLogfileWindowState() {
      this.renderedLines = [];
      this.displayLines = [];
      this.clearRemainderStates();
      this.loadedBytes = 0;
      this.windowStart = 0;
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
            from(this.instance.fetchLogfileRange(0, 0)).pipe(
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
      this.renderedLines = this.splitLines(response.data, response.windowStart);
      this.windowStart = response.windowStart;
      this.windowEnd = response.windowEnd;
      this.totalBytes = response.totalBytes;
      this.loadedBytes = this.calculateLoadedBytes();
      if (ChunkDirection.REPLACE === direction) {
        this.clearRemainderStates();
        if (this.windowStart > 0) {
          const headLine = this.renderedLines[0];
          if (headLine !== undefined && headLine.text !== '') {
            this.setHeadRemainderLine(headLine);
          }
          this.renderedLines.shift();
        }
        const tailLine = this.renderedLines.at(-1);
        if (tailLine !== undefined && tailLine.text !== '') {
          this.setTailRemainderLine(tailLine);
        }
        if (this.renderedLines.length > 0) {
          this.renderedLines.pop();
        }
        this.displayLines = this.renderedLines;
      } else if (ChunkDirection.NEXT === direction) {
        if (
          this.tailRemainderState.presentOrnotBoolean &&
          this.renderedLines.length > 0
        ) {
          this.renderedLines[0] = this.mergeTailRemainderWithLine(
            this.renderedLines[0],
          );
          this.clearTailRemainderState();
        }
        this.clearHeadRemainderState();
        const tailLine = this.renderedLines.at(-1);
        if (tailLine && tailLine.text !== '') {
          this.setTailRemainderLine(tailLine);
        }
        if (this.renderedLines.length > 0) {
          this.renderedLines.pop();
        }
        this.displayLines = this.renderedLines;
      } else if (ChunkDirection.PREVIOUS === direction) {
        if (
          this.headRemainderState.presentOrnotBoolean &&
          this.renderedLines.length > 0
        ) {
          let lastIndex = this.renderedLines.length - 1;
          this.renderedLines[lastIndex] = this.mergeLineWithHeadRemainder(
            this.renderedLines[lastIndex],
          );
          this.clearHeadRemainderState();
        }
        this.clearTailRemainderState();
        if (this.windowStart > 0) {
          const headLine = this.renderedLines[0];
          if (headLine && headLine.text !== '') {
            this.setHeadRemainderLine(headLine);
          }
          this.renderedLines.shift();
        }
        this.displayLines = this.renderedLines;
      }
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
        const response = await this.instance.fetchLogfileRange(start, end);
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
      let end = this.windowStart - 1;
      const start = Math.max(0, end - this.chunkSize + 1);
      if (start === 0) {
        end = Math.min(this.totalBytes - 1, this.chunkSize - 1);
      }
      const direction =
        end < this.windowStart
          ? ChunkDirection.PREVIOUS
          : ChunkDirection.REPLACE;
      await this.loadChunk(start, end, direction);
    },
    async loadNextChunk() {
      const currentWindowEnd = this.windowEnd;
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
