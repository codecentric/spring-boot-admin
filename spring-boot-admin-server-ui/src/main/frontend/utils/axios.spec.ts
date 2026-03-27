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

import {
  addLanguageHeaderInterceptor,
  redirectOn401,
  registerErrorToastInterceptor,
} from './axios';

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
    const error: AxiosError = {
      response: {
        status: 500,
      },
    } as AxiosError;

    try {
      await redirectOn401()(error);
    } catch (e) {
      expect(e).toBe(error);
    }

    expect(window.location.assign).not.toBeCalled();
  });

  it('should redirect on 401', async () => {
    const error: AxiosError = {
      response: {
        status: 401,
      },
    } as AxiosError;

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
    const error: AxiosError = {
      response: {
        status: 401,
      },
    } as AxiosError;

    try {
      await redirectOn401(() => false)(error);
    } catch (e) {
      expect(e).toBe(error);
    }

    expect(window.location.assign).not.toBeCalled();
  });
});

describe('addLanguageHeaderInterceptor', () => {
  beforeEach(() => {
    // Clear globalThis.SBA before each test
    if (globalThis.SBA) {
      delete globalThis.SBA;
    }
  });

  it('should include selected language with highest priority', () => {
    globalThis.SBA = {
      useI18n: () => ({ locale: { value: 'de' } }),
    } as any;

    const config = {
      url: '/api/test',
      headers: {},
    };

    const result = addLanguageHeaderInterceptor(config);

    expect(result.headers['Accept-Language']).toContain('de;q=1.0');
  });

  it('should include navigator language with lower priority', () => {
    globalThis.SBA = {
      useI18n: () => ({ locale: { value: 'de' } }),
    } as any;

    // Mock navigator.language
    Object.defineProperty(navigator, 'language', {
      value: 'en-US',
      configurable: true,
    });

    const config = {
      url: '/api/test',
      headers: {},
    };

    const result = addLanguageHeaderInterceptor(config);

    expect(result.headers['Accept-Language']).toContain('de;q=1.0');
    expect(result.headers['Accept-Language']).toContain('en-US;q=0.9');
  });

  it('should not duplicate navigator language if it matches selected language', () => {
    globalThis.SBA = {
      useI18n: () => ({ locale: { value: 'de' } }),
    } as any;

    // Mock navigator.language to match selected language
    Object.defineProperty(navigator, 'language', {
      value: 'de',
      configurable: true,
    });

    const config = {
      url: '/api/test',
      headers: {},
    };

    const result = addLanguageHeaderInterceptor(config);

    // Should only include de once
    const languageHeader = result.headers['Accept-Language'];
    const deCount = (languageHeader.match(/de;q=1\.0/g) || []).length;
    expect(deCount).toBe(1);
  });

  it('should include wildcard fallback', () => {
    globalThis.SBA = {
      useI18n: () => ({ locale: { value: 'fr' } }),
    } as any;

    const config = {
      url: '/api/test',
      headers: {},
    };

    const result = addLanguageHeaderInterceptor(config);

    expect(result.headers['Accept-Language']).toContain('*;q=0.8');
  });

  it('should handle missing globalThis.SBA gracefully', () => {
    // Don't set globalThis.SBA - it should handle this gracefully
    const config = {
      url: '/api/test',
      headers: {},
    };

    const result = addLanguageHeaderInterceptor(config);

    // Should return the config unchanged if SBA is not available
    expect(result).toBe(config);
    expect(result.headers['Accept-Language']).toBeUndefined();
  });

  it('should preserve existing headers when adding Accept-Language', () => {
    globalThis.SBA = {
      useI18n: () => ({ locale: { value: 'fr' } }),
    } as any;

    const config = {
      url: '/api/test',
      headers: {
        Authorization: 'Bearer token',
        'Content-Type': 'application/json',
      },
    };

    const result = addLanguageHeaderInterceptor(config);

    expect(result.headers['Authorization']).toBe('Bearer token');
    expect(result.headers['Content-Type']).toBe('application/json');
    expect(result.headers['Accept-Language']).toContain('fr;q=1.0');
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
