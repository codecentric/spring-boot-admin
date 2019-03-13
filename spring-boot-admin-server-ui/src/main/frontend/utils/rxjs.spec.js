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

import {concat, EMPTY, of, throwError} from '@/utils/rxjs';
import {delay, doOnSubscribe, listen} from './rxjs';

describe('doOnSubscribe', () => {
  it('should call callback when subscribing', done => {
    const cb = jest.fn();
    EMPTY.pipe(
      doOnSubscribe(cb)
    ).subscribe({
      complete: () => {
        expect(cb).toHaveBeenCalledTimes(1);
        done();
      }
    });
  });
});

describe('listen', () => {
  it('should call callback with complete', done => {
    const cb = jest.fn();
    EMPTY.pipe(
      listen(cb)
    ).subscribe({
      complete: () => {
        expect(cb).toHaveBeenCalledTimes(1);
        expect(cb).toHaveBeenCalledWith('completed');
        done();
      }
    });
  });

  it('should call callback with executing and complete', done => {
    const cb = jest.fn();
    of(1).pipe(
      delay(10),
      listen(cb, 1)
    ).subscribe({
      complete: () => {
        expect(cb).toHaveBeenCalledTimes(2);
        expect(cb).toHaveBeenCalledWith('executing');
        expect(cb).toHaveBeenCalledWith('completed');
        done();
      }
    });
  });

  it('should call callback with failed', done => {
    const cb = jest.fn();
    throwError(new Error('test')).pipe(
      listen(cb)
    ).subscribe({
      error: () => {
        expect(cb).toHaveBeenCalledTimes(1);
        expect(cb).toHaveBeenCalledWith('failed');
        done();
      }
    });
  });

  it('should call callback with executing and failed', done => {
    const cb = jest.fn();

    concat(
      of(1).pipe(delay(10)),
      throwError(new Error('test'))
    ).pipe(
      listen(cb, 1)
    ).subscribe({
      error: () => {
        expect(cb).toHaveBeenCalledTimes(2);
        expect(cb).toHaveBeenCalledWith('executing');
        expect(cb).toHaveBeenCalledWith('failed');
        done();
      }
    });
  });

});
