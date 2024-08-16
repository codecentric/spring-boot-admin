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

const isStorybook = Object.prototype.hasOwnProperty.call(window, 'STORIES');

const views = [];

if (!isStorybook) {
  const context: Record<string, any> = import.meta.glob(
    './**/index.(js|vue|ts)',
    { eager: true },
  );
  Object.keys(context)
    .filter((key) => {
      const contextElement = context[key];
      return 'default' in contextElement;
    })
    .forEach(function (key) {
      const defaultExport = context[key].default;
      if (defaultExport && defaultExport.install) {
        views.push(defaultExport);
      }
    });
}

export default views;
