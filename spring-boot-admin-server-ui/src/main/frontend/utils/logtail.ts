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

export enum StreamType {
  Data = 'data',
  Reset = 'reset',
  Empty = 'empty',
}

const parseInteger = (value, fallback) => {
  const parsed = parseInt(value, 10);
  return Number.isNaN(parsed) ? fallback : parsed;
};

export const getTotalBytesFrom416 = (response) => {
  const contentRange = response.headers?.get?.('content-range');
  const match = contentRange?.match(/^bytes\s+\*\/(\d+)$/i);

  return match ? parseInteger(match[1], undefined) : undefined;
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
  let streamUpdated = true;

  return timer(0, interval).pipe(
    concatMap(() => {
      return new Observable((observer) => {
        getFn({
          responseType: 'text',
          headers: { range, Accept: 'text/plain' },
        })
          .then((response) => {
            streamUpdated = false;
            observer.next(response);
            observer.complete();
          })
          .catch((error) => observer.error(error));
      }).pipe(
        catchError((error) =>
          of({
            data: '',
            status: error.response?.status,
            headers: error.response?.headers,
          }),
        ),
      );
    }),
    concatMap((response) => {
      //resetting when log file is compressed
      if (response.status === 416) {
        const currentSize = getTotalBytesFrom416(response);
        if (currentSize === size) {
          return EMPTY;
        }
        size = 0;
        range = `bytes=-${initialSize}`;
        streamUpdated = true;
        return of({ type: StreamType.Reset });
      }
      const { windowStart, windowEnd, totalBytes } =
        getLogfileWindowMetadata(response);
      if (response.status === 206 || response.status === 200) {
        if (totalBytes > size) {
          streamUpdated = true;
          size = totalBytes;
          range = `bytes=${Math.max(size, 0)}-`;
        }
      } else {
        throw 'Unexpected response status: ' + response.status;
      }
      if (size === 0) {
        return of({ type: StreamType.Empty });
      }
      const addendum = response.data;

      if (streamUpdated) {
        return of({
          type: StreamType.Data,
          totalBytes: size,
          addendum,
          windowStart,
          windowEnd,
        });
      } else {
        return EMPTY;
      }
    }),
  );
};
