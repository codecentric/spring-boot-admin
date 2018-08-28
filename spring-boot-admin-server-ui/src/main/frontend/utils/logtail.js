/*
 * Copyright 2014-2018 the original author or authors.
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

import {concatMap, EMPTY, of, timer} from '@/utils/rxjs';

export default (getFn, interval, initialSize = 300 * 1024) => {
  let range = `bytes=-${initialSize}`;
  let size = 0;

  return timer(0, interval)
    .pipe(
      concatMap(() => getFn({headers: {range, 'Accept': 'text/plain, */*'}})),
      concatMap(response => {
        const initial = size === 0;
        const contentLength = response.data.length;
        if (response.status === 200) {
          if (!initial) {
            throw 'Expected 206 - Partial Content on subsequent requests.';
          }
          size = contentLength;
        } else if (response.status === 206) {
          size = parseInt(response.headers['content-range'].split('/')[1]);
        } else {
          throw 'Unexpected response status: ' + response.status;
        }

        // Reload the last byte to avoid a 416: Range unsatisfiable.
        // If the response has length = 1 the file hasn't beent changed.
        // If the response status is 416 the logfile has been truncated.
        range = `bytes=${size - 1}-`;

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
          // Remove the first byte which has been part of the previos response.
          addendum = response.data.substring(1);
        }

        return addendum ? of({
          totalBytes: size,
          skipped,
          addendum
        }) : EMPTY;
      })
    );
}
