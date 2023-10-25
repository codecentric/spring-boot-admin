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
import axios from 'axios';

import sbaConfig from '../sba-config';

const nc = useNotificationCenter();

axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
axios.defaults.xsrfHeaderName = sbaConfig.csrf.headerName;

export const redirectOn401 =
  (predicate = () => true) =>
  (error) => {
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
axios.interceptors.response.use((response) => response, redirectOn401());

export default axios;

export const registerErrorToastInterceptor = (axios) => {
  if (sbaConfig.uiSettings.enableToasts === true) {
    axios.interceptors.response.use(
      (response) => response,
      (error) => {
        const data = error.request;
        const message = `
                Request failed: ${data.statusText}<br>
                <small>${data.responseURL}</small>
        `;
        nc.error(message, {
          context: data.status ?? 'axios',
          title: `Error ${data.status}`,
          duration: 10000,
        });
      },
    );
  }
};
