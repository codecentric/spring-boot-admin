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

import 'rxjs/add/observable/defer';
import 'rxjs/add/observable/empty';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/timer';
import 'rxjs/add/operator/concat';
import 'rxjs/add/operator/concatAll';
import 'rxjs/add/operator/concatMap';
import 'rxjs/add/operator/delay';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/filter';
import 'rxjs/add/operator/ignoreElements';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/retryWhen';

import {Observable} from 'rxjs/Observable';
import {animationFrame} from 'rxjs/scheduler/animationFrame';

Observable.prototype.doOnSubscribe = function (onSubscribe) {
  let source = this;
  return Observable.defer(() => {
    onSubscribe();
    return source;
  });
};

Observable.prototype.doFirst = function (doFirst) {
  let source = this;
  let triggered = false;
  return Observable.defer(() => {
    triggered = false;
    return source;
  }).do(n => {
    if (!triggered) {
      triggered = true;
      doFirst(n);
    }
  });
};

Observable.prototype.listen = function (callbackFn) {
  let handle = null;
  return this.doOnSubscribe(() => handle = setTimeout(() => callbackFn('executing'), 150))
    .do({
      complete: () => {
        handle && clearTimeout(handle);
        callbackFn('completed');
      },
      error: (error) => {
        console.warn('Operation failed:', error);
        handle && clearTimeout(handle);
        callbackFn('failed');
      }
    });
};

export {
  Observable,
  animationFrame
};
