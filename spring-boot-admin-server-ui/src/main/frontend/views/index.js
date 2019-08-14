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

const views = [];

/* global require */
const context = require.context('.', true, /^\.\/.+\/index\.(js|vue)$/);
context.keys().forEach(function (key) {
  const defaultExport = context(key).default;
  if (defaultExport && defaultExport.install) {
    views.push(defaultExport)
  }
});

export const VIEW_GROUP = {
  WEB: 'web',
  INSIGHTS: 'insights',
  DATA: 'data',
  JVM: 'jvm',
  LOGGING: 'logging',
  NONE: 'none',
  SECURITY: 'security'
};

export default views;
