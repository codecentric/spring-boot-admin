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

//This is a Thymleaf template whill will be rendered by the backend
var SBA = {
  uiSettings: /*[[${uiSettings}]]*/ {},
  user: /*[[${user}]]*/ null,
  extensions: [],
  csrf: {
    parameterName: /*[[${_csrf} ? ${_csrf.parameterName} : 'null']]*/ null,
    headerName: /*[[${_csrf} ? ${_csrf.headerName} : 'null']]*/ null
  },
  use: function (ext) {
    this.extensions.push(ext);
  }
};
