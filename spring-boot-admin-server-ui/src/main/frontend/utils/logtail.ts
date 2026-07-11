/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import {
  EMPTY,
  Observable,
  catchError,
  concatMap,
  of,
  throwError,
  timer,
} from './rxjs';

export const DEFAULT_LOGFILE_CHUNK_SIZE = 300 * 1024;

export enum StreamType {
  Data = 'data',
  Reset = 'reset',
  Empty = 'empty',
}

export const ChunkDirection = Object.freeze({
  PREVIOUS: 'previous',
  NEXT: 'next',
  REPLACE: 'replace',
});

export enum ContentType {
  NormalContent = 'normalContent',
  ShortContent = 'shortContent',
}

const parseInteger = (value, fallback) => {
  const parsed = parseInt(value, 10);
  return Number.isNaN(parsed) ? fallback : parsed;
};

const textEncoder = new TextEncoder();
const textDecoder = new TextDecoder();

const byteLength = (content) => textEncoder.encode(content).length;
const substringFromByteOffset = (content, offset) =>
  textDecoder.decode(textEncoder.encode(content).slice(offset));

export const getTotalBytesFrom416 = (response) => {
  const contentRange = response.headers?.get?.('content-range');
  const match = contentRange?.match(/^bytes\s+\*\/(\d+)$/i);

  return match ? parseInteger(match[1], undefined) : undefined;
};

export const getLogfileWindowMetadata = (response) => {
  const contentLength = byteLength(response.data);
  const contentRange = response.headers.get('content-range');
  const rangeMatch = contentRange?.match(/^bytes\s+(\d+)-(\d+)\/(\d+|\*)$/i);

  if (rangeMatch) {
    return {
      windowStart: parseInteger(rangeMatch[1], 0),
      windowEnd: parseInteger(rangeMatch[2], Math.max(contentLength - 1, 0)),
      totalBytes: parseInteger(rangeMatch[3], contentLength),
    };
  }

  const totalBytes = parseInteger(
    response.headers.get('content-length'),
    contentLength,
  );

  return {
    windowStart: 0,
    windowEnd: Math.max(contentLength - 1, 0),
    totalBytes,
  };
};

export const TrimtoCompleteLines = (
  content: string,
  windowStart: number,
  windowEnd: number,
  firstChunkSet: boolean,
) => {
  const completeLineStart = content.indexOf('\n');
  const completeLineEnd = content.lastIndexOf('\n');

  if (completeLineEnd === -1) {
    return {
      trimmedCompleteLines: firstChunkSet ? content : undefined,
      windowStart,
      windowEnd,
      contentType: ContentType.ShortContent,
    };
  }

  let trimmedStart = 0;
  if (!firstChunkSet && windowStart > 0) {
    if (completeLineStart === completeLineEnd) {
      return {
        trimmedCompleteLines: undefined,
        windowStart,
        windowEnd,
        contentType: ContentType.ShortContent,
      };
    }
    trimmedStart = completeLineStart + 1;
  }

  const trimmedCompleteLines = content.substring(
    trimmedStart,
    completeLineEnd + 1,
  );
  const newWindowEnd =
    windowEnd - byteLength(content.substring(completeLineEnd + 1));
  const newWindowStart = newWindowEnd - byteLength(trimmedCompleteLines) + 1;

  return {
    trimmedCompleteLines,
    windowStart: newWindowStart,
    windowEnd: newWindowEnd,
    contentType: ContentType.NormalContent,
  };
};

export default (getFn, interval, initialSize = DEFAULT_LOGFILE_CHUNK_SIZE) => {
  let range = `bytes=-${initialSize}`;
  let size = 0;
  let lastCompleteByte = -1;
  let firstChunkSet = false;

  return timer(0, interval).pipe(
    concatMap(() => {
      return new Observable((observer) => {
        getFn({
          responseType: 'text',
          headers: { range, Accept: 'text/plain' },
        })
          .then((response) => {
            observer.next(response);
            observer.complete();
          })
          .catch((error) => observer.error(error));
      }).pipe(
        catchError((error) => {
          if (error.response?.status !== 416) {
            return throwError(() => error);
          }
          return of({
            data: '',
            status: error.response?.status,
            headers: error.response?.headers,
          });
        }),
      );
    }),
    concatMap((response) => {
      //resetting when log file is compressed
      if (response.status === 416) {
        const currentSize = getTotalBytesFrom416(response);
        if (currentSize === size) {
          return of({ type: StreamType.Empty });
        }
        range = `bytes=-${initialSize}`;
        size = 0;
        lastCompleteByte = -1;
        firstChunkSet = false;
        return of({ type: StreamType.Reset });
      }
      const { windowStart, windowEnd, totalBytes } =
        getLogfileWindowMetadata(response);
      const overlap = firstChunkSet
        ? Math.max(lastCompleteByte - windowStart + 1, 0)
        : 0;
      let addendum = substringFromByteOffset(response.data, overlap);
      let addendumWindowStart = windowStart + overlap;
      let addendumWindowEnd = windowEnd;
      if (response.status === 206 || response.status === 200) {
        if (totalBytes > size) {
          const trimmed = TrimtoCompleteLines(
            addendum,
            addendumWindowStart,
            windowEnd,
            firstChunkSet,
          );
          if (trimmed.contentType === ContentType.ShortContent) {
            return EMPTY;
          }
          if (!firstChunkSet) {
            firstChunkSet = true;
          }
          addendum = trimmed.trimmedCompleteLines;
          addendumWindowStart = trimmed.windowStart;
          addendumWindowEnd = trimmed.windowEnd;
          size = totalBytes;
          lastCompleteByte = trimmed.windowEnd;
          range = `bytes=${lastCompleteByte}-`;
          return of({
            type: StreamType.Data,
            totalBytes: size,
            addendum,
            windowStart: addendumWindowStart,
            windowEnd: addendumWindowEnd,
          });
        } else {
          return EMPTY;
        }
      } else {
        throw 'Unexpected response status: ' + response.status;
      }
    }),
  );
};

export const fetchLogfileRange = async (instance, start, end, direction) => {
  const { data, totalBytes, windowStart, windowEnd, status } =
    await instance.fetchLogfileRange(start, end);
  //manual polling return type
  if (start == 0 && end == 0) {
    return {
      data,
      totalBytes,
      windowStart,
      windowEnd,
      status,
    };
  }
  const completeLineStart = data.indexOf('\n');
  const completeLineEnd = data.lastIndexOf('\n');
  const hasAtLeastTwoNewLines =
    completeLineStart !== -1 && completeLineStart !== completeLineEnd;
  if (!hasAtLeastTwoNewLines) {
    throw new Error(
      'Too few lines: need at least two lines to display properly',
    );
  }
  if (ChunkDirection.NEXT === direction) {
    const trimmedCompleteLines = data.substring(0, completeLineEnd + 1);
    const newWindowEnd =
      windowEnd - byteLength(data.substring(completeLineEnd + 1));
    return {
      data: trimmedCompleteLines,
      totalBytes,
      windowStart,
      windowEnd: newWindowEnd,
      status,
    };
  }
  if (windowStart === 0) {
    const trimmedCompleteLines = data.substring(0, completeLineEnd + 1);
    const newWindowEnd =
      windowEnd - byteLength(data.substring(completeLineEnd + 1));
    return {
      data: trimmedCompleteLines,
      totalBytes,
      windowStart,
      windowEnd: newWindowEnd,
      status,
    };
  } else {
    const trimmedCompleteLines = data.substring(
      completeLineStart + 1,
      completeLineEnd + 1,
    );
    const newWindowEnd =
      windowEnd - byteLength(data.substring(completeLineEnd + 1));
    const newWindowStart = newWindowEnd - byteLength(trimmedCompleteLines) + 1;
    return {
      data: trimmedCompleteLines,
      totalBytes,
      windowStart: newWindowStart,
      windowEnd: newWindowEnd,
      status,
    };
  }
};
