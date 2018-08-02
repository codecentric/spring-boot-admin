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

import {redirectOn401} from './axios';

describe('redirectOn401', () => {
  it('should not redirect on 500', async () => {
    window.location.assign = jest.fn();

    const error = {
      response: {
        status: 500
      }
    };

    try {
      await redirectOn401()(error);
    } catch (e) {
      expect(e).toBe(error);
    }

    expect(window.location.assign).not.toBeCalled();
  });

  it('should redirect on 401', async () => {
    window.location.assign = jest.fn();

    const error = {
      response: {
        status: 401
      }
    };

    try {
      await redirectOn401()(error);
    } catch (e) {
      expect(e).toBe(error);
    }

    expect(window.location.assign).toBeCalledWith('login?redirectTo=http%3A%2F%2Fexample.com%2F');
  });

  it('should not redirect on 401 for predicate yields false', async () => {
    window.location.assign = jest.fn();

    const error = {
      response: {
        status: 401
      }
    };

    try {
      await redirectOn401(() => false)(error);
    } catch (e) {
      expect(e).toBe(error);
    }

    expect(window.location.assign).not.toBeCalled();
  });
});
