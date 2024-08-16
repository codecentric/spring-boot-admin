/*
 * Copyright 2014-2024 the original author or authors.
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
import { merge } from 'lodash-es';

const brand =
  '<img src="assets/img/icon-spring-boot-admin.svg">Spring Boot Admin';

const DEFAULT_CONFIG = {
  uiSettings: {
    brand,
    theme: {
      backgroundEnabled: true,
      color: '#42d3a5',
    },
    notifications: {
      enabled: true,
    },
    rememberMeEnabled: true,
    externalViews: [] as ExternalView[],
    favicon: 'assets/img/favicon.png',
    faviconDanger: 'assets/img/favicon-danger.png',
    notificationFilterEnabled: false,
    routes: [],
    availableLanguages: [],
    viewSettings: [],
    pollTimer: {
      cache: 2500,
      datasource: 2500,
      gc: 2500,
      process: 2500,
      memory: 2500,
      threads: 2500,
      logfile: 1000,
    },
  },
  user: null,
  extensions: [],
  csrf: {
    parameterName: '_csrf',
    headerName: 'X-XSRF-TOKEN',
  },
  use: function (ext) {
    this.extensions.push(ext);
  },
};

const mergedConfig = merge(DEFAULT_CONFIG, window.SBA);

export const getCurrentUser = () => {
  return mergedConfig.user;
};

export default mergedConfig;
