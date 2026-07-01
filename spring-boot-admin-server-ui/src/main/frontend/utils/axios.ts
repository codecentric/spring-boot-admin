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
import { useNotificationCenter } from '@stekoe/vue-toast-notificationcenter';
import axios, {
  type AxiosError,
  AxiosInstance,
  type InternalAxiosRequestConfig,
} from 'axios';

import sbaConfig from '../sba-config';

const nc = useNotificationCenter();

axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
axios.defaults.xsrfHeaderName = sbaConfig.csrf.headerName;

export const redirectOn401 =
  (predicate: (error: AxiosError) => boolean = () => true) =>
  (error: AxiosError) => {
    if (error.response && error.response.status === 401 && predicate(error)) {
      window.location.assign(
        `login?redirectTo=${encodeURIComponent(
          window.location.href,
        )}&error=401`,
      );
    }
    return Promise.reject(error);
  };

axios.defaults.withCredentials = true;
axios.defaults.headers.common['Accept'] = 'application/json';

/**
 * Adds Accept-Language header to requests with user's selected UI language preference.
 * Format: selected-language;q=1.0, navigator-language;q=0.9, *;q=0.8
 *
 * @param config The axios request configuration
 * @returns The modified request configuration
 */
export const addLanguageHeaderInterceptor = (
  config: InternalAxiosRequestConfig,
): InternalAxiosRequestConfig => {
  try {
    const i18n = globalThis.SBA?.useI18n?.();
    if (i18n?.locale.value) {
      const selectedLanguage = i18n.locale.value;
      const navigatorLanguage = navigator.language;

      // Build Accept-Language header with selected UI language as primary preference
      const acceptLanguageParts = [
        `${selectedLanguage};q=1.0`, // Selected UI language - highest priority
      ];

      // Add navigator language if it's different from selected language
      if (
        navigatorLanguage !== selectedLanguage &&
        !navigatorLanguage.startsWith(selectedLanguage)
      ) {
        acceptLanguageParts.push(`${navigatorLanguage};q=0.9`);
      }

      // Add wildcard fallback for any other language
      acceptLanguageParts.push('*;q=0.8');

      config.headers['Accept-Language'] = acceptLanguageParts.join(', ');
    }
  } catch (error) {
    // Log in development mode for debugging
    if (process.env.NODE_ENV === 'development') {
      console.error('Failed to add language header:', error);
    }
    // Silently fail in production if i18n is not yet initialized
  }
  return config;
};

axios.interceptors.request.use(addLanguageHeaderInterceptor);

axios.interceptors.response.use((response) => response, redirectOn401());

export default axios;

export const registerErrorToastInterceptor = (
  axios: AxiosInstance,
  notificationCenter = nc,
): void => {
  if (sbaConfig.uiSettings.enableToasts) {
    axios.interceptors.response.use(
      (response) => response,
      (error: AxiosError) => {
        const suppress = error.config?.suppressToast;
        let shouldSuppress: boolean;
        if (typeof suppress === 'function') {
          shouldSuppress = suppress(error);
        } else {
          shouldSuppress = !!suppress;
        }
        if (!shouldSuppress) {
          const data = error.response;
          const message = `
                Request failed: ${data?.statusText}<br>
                <small>${data?.config?.url || data?.request?.responseURL || ''}</small>
        `;
          notificationCenter.error(message, {
            context: data?.status ?? 'axios',
            title: `Error ${data?.status}`,
            duration: 10000,
          });
        }
        return Promise.reject(error);
      },
    );
  }
};
