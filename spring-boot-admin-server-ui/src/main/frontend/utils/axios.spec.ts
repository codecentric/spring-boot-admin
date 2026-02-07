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
import axios, { AxiosError } from 'axios';
import MockAdapter from 'axios-mock-adapter';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { redirectOn401, registerErrorToastInterceptor } from './axios';

// Initialize errorSpy BEFORE any mocks or imports
globalThis.errorSpy = vi.fn();

// Mock sba-config to enable toasts
vi.mock('../sba-config', () => ({
  default: {
    uiSettings: {
      enableToasts: true,
    },
    csrf: {
      parameterName: '_csrf',
      headerName: 'X-XSRF-TOKEN',
    },
  },
}));

// Use manual mock for @stekoe/vue-toast-notificationcenter
vi.mock('@stekoe/vue-toast-notificationcenter');

describe('redirectOn401', () => {
  beforeEach(() => {
    Object.defineProperty(window, 'location', {
      writable: true,
      value: {
        assign: vi.fn(),
        href: 'http://example.com/',
      },
    });
  });

  it('should not redirect on 500', async () => {
    const error = {
      response: {
        status: 500,
      },
    };

    try {
      await redirectOn401()(error);
    } catch (e) {
      expect(e).toBe(error);
    }

    expect(window.location.assign).not.toBeCalled();
  });

  it('should redirect on 401', async () => {
    const error = {
      response: {
        status: 401,
      },
    };

    try {
      await redirectOn401()(error);
    } catch (e) {
      expect(e).toBe(error);
    }

    expect(window.location.assign).toBeCalledWith(
      'login?redirectTo=http%3A%2F%2Fexample.com%2F&error=401',
    );
  });

  it('should not redirect on 401 for predicate yields false', async () => {
    const error = {
      response: {
        status: 401,
      },
    };

    try {
      await redirectOn401(() => false)(error);
    } catch (e) {
      expect(e).toBe(error);
    }

    expect(window.location.assign).not.toBeCalled();
  });
});

describe('registerErrorToastInterceptor', () => {
  let axiosInstance;
  let mock;

  beforeEach(() => {
    globalThis.errorSpy.mockClear();
    axiosInstance = axios.create();
    // Pass a mock notification center directly
    registerErrorToastInterceptor(axiosInstance, {
      error: globalThis.errorSpy,
    });
    mock = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    if (mock) mock.restore();
    vi.restoreAllMocks();
  });

  it('shows toast by default', async () => {
    mock.onGet('/fail').reply(500);
    await expect(axiosInstance.get('/fail')).rejects.toBeDefined();
    expect(globalThis.errorSpy).toHaveBeenCalled();
  });

  it('suppresses toast if suppressToast is true', async () => {
    mock.onGet('/fail').reply(500);
    await expect(
      axiosInstance.get('/fail', { suppressToast: true }),
    ).rejects.toBeDefined();
    expect(globalThis.errorSpy).not.toHaveBeenCalled();
  });

  it('shows toast if suppressToast is false', async () => {
    mock.onGet('/fail').reply(500);
    await expect(
      axiosInstance.get('/fail', { suppressToast: false }),
    ).rejects.toBeDefined();
    expect(globalThis.errorSpy).toHaveBeenCalled();
  });

  it('suppresses toast if suppressToast function returns true', async () => {
    mock.onGet('/fail').reply(404);
    const suppressFn = (err: AxiosError) => err.response?.status === 404;
    await expect(
      axiosInstance.get('/fail', { suppressToast: suppressFn }),
    ).rejects.toBeDefined();
    expect(globalThis.errorSpy).not.toHaveBeenCalled();
  });

  it('shows toast if suppressToast function returns false', async () => {
    mock.onGet('/fail').reply(500);
    const suppressFn = (err: AxiosError) => err.response?.status === 404;
    await expect(
      axiosInstance.get('/fail', { suppressToast: suppressFn }),
    ).rejects.toBeDefined();
    expect(globalThis.errorSpy).toHaveBeenCalled();
  });
});
