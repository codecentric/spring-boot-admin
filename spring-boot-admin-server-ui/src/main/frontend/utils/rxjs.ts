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
import { defer, tap } from 'rxjs';

export {
  of,
  defer,
  concat,
  catchError,
  throwError,
  EMPTY,
  from,
  timer,
  Observable,
  Subject,
  animationFrameScheduler,
  concatMap,
  delay,
  debounceTime,
  mergeWith,
  map,
  retryWhen,
  tap,
  filter,
  concatAll,
  ignoreElements,
  bufferTime,
  finalize,
} from 'rxjs';

export const doOnSubscribe = (cb) => (source) =>
  defer(() => {
    cb();
    return source;
  });

export const listen =
  (cb, execDelay = 150) =>
  (source) => {
    let handle = null;
    return source.pipe(
      doOnSubscribe(
        () => (handle = setTimeout(() => cb('executing'), execDelay)),
      ),
      tap({
        complete: () => {
          clearTimeout(handle);
          cb('completed');
        },
        error: (error) => {
          console.warn('Operation failed:', error);
          clearTimeout(handle);
          cb('failed');
        },
      }),
    );
  };
