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

import merge from 'lodash/merge';

const DEFAULT_CONFIG = {
  uiSettings: {
    brand: '<img src="assets/img/icon-spring-boot-admin.svg"><span>Spring Boot Admin</span>',
    rememberMeEnabled: true,
    externalViews: [],
    favicon: 'assets/img/favicon.png',
    faviconDanger: 'assets/img/favicon-danger.png',
    notificationFilterEnabled: false,
    routes: [],
  },
  user: null,
  extensions: [],
  externalViews: [],
  csrf: {
    parameterName: '_csrf',
    headerName: 'X-XSRF-TOKEN'
  },
  use: function (ext) {
    this.extensions.push(ext);
  }
};

export default merge(DEFAULT_CONFIG, global.SBA)
