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
import { EMPTY, Observable, catchError, concatMap, of, timer } from './rxjs';

export const DEFAULT_LOGFILE_CHUNK_SIZE = 300 * 1024;

const parseInteger = (value, fallback) => {
  const parsed = parseInt(value, 10);
  return Number.isNaN(parsed) ? fallback : parsed;
};

export const getLogfileWindowMetadata = (response) => {
  const contentLength = response.data.length;
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

export default (getFn, interval, initialSize = DEFAULT_LOGFILE_CHUNK_SIZE) => {
  let range = `bytes=-${initialSize}`;
  let size = 0;
  let atTheEnd = false;

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
        catchError((error) => of({ data: '', status: error.response.status })),
      );
    }),
    concatMap((response) => {
      let initial = size === 0;
      const contentLength = response.data.length;
      let windowStart = 0;
      let windowEnd = Math.max(contentLength - 1, 0);

      if (response.status === 200) {
        if (!initial) {
          throw 'Expected 206 - Partial Content on subsequent requests.';
        }
        size = contentLength;
        range = `bytes=${Math.max(size - 1, 0)}-`;
      } else if (response.status === 206) {
        const metadata = getLogfileWindowMetadata(response);
        size = metadata.totalBytes;
        windowStart = metadata.windowStart;
        windowEnd = metadata.windowEnd;
        // The end value of the range is always one byte less than the size when at the end
        atTheEnd = windowEnd === size - 1;
        range = `bytes=${Math.max(size - 1, 0)}-`;
      } else if (response.status === 416) {
        size = 0;
        range = `bytes=-${initialSize}`;
        initial = true;
      } else {
        throw 'Unexpected response status: ' + response.status;
      }

      let addendum = null;
      let skipped = 0;

      if (initial) {
        if (contentLength >= size) {
          addendum = response.data;
        } else {
          // In case of a partial response find the first line break.
          addendum = response.data.substring(response.data.indexOf('\n') + 1);
          skipped = size - addendum.length;
        }
      } else if (response.data.length > 1) {
        // Remove the first byte which has been part of the previous response.
        addendum = response.data.substring(1);
      }

      return addendum
        ? of({
            totalBytes: size,
            skipped,
            // The log file always temporarily ends with a new line until the next one is written.
            // Therefore, if we're at the end of it, we drop such a new line.
            addendum: atTheEnd ? addendum.trimEnd() : addendum,
            windowStart,
            windowEnd,
          })
        : EMPTY;
    }),
  );
};
