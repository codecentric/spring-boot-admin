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

import {defer} from 'rxjs/internal/observable/defer';
import {tap} from 'rxjs/internal/operators/tap';

export {throwError} from 'rxjs/internal/observable/throwError';
export {of} from 'rxjs/internal/observable/of';
export {defer} from 'rxjs/internal/observable/defer';
export {concat} from 'rxjs/internal/observable/concat';
export {EMPTY} from 'rxjs/internal/observable/empty';
export {from} from 'rxjs/internal/observable/from';
export {timer} from 'rxjs/internal/observable/timer';
export {Observable} from 'rxjs/internal/Observable';
export {Subject} from 'rxjs/internal/Subject';
export {animationFrame as animationFrameScheduler} from 'rxjs/internal/scheduler/animationFrame';
export {concatMap} from 'rxjs/internal/operators/concatMap';
export {delay} from 'rxjs/internal/operators/delay';
export {debounceTime} from 'rxjs/internal/operators/debounceTime';
export {merge} from 'rxjs/internal/operators/merge';
export {map} from 'rxjs/internal/operators/map';
export {retryWhen} from 'rxjs/internal/operators/retryWhen';
export {tap} from 'rxjs/internal/operators/tap';
export {filter} from 'rxjs/internal/operators/filter';
export {concatAll} from 'rxjs/internal/operators/concatAll';
export {ignoreElements} from 'rxjs/internal/operators/ignoreElements';
export {bufferTime} from 'rxjs/internal/operators/bufferTime';
export {finalize} from 'rxjs/internal/operators/finalize';

export const doOnSubscribe = cb => source =>
  defer(() => {
    cb();
    return source
  });

export const listen = (cb, execDelay = 150) => source => {
  let handle = null;
  return source.pipe(
    doOnSubscribe(() => handle = setTimeout(() => cb('executing'), execDelay)),
    tap({
      complete: () => {
        handle && clearTimeout(handle);
        cb('completed');
      },
      error: (error) => {
        console.warn('Operation failed:', error);
        handle && clearTimeout(handle);
        cb('failed');
      }
    })
  )
};
