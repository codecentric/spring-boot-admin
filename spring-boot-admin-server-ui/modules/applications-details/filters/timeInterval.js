/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

module.exports = function () {
  function padZero(i, n) {
    var s = i + '';
    while (s.length < n) {
      s = '0' + s;
    }
    return s;
  }

  return function (input) {
    var s = input || 0;
    var d = padZero(Math.floor(s / 86400000), 2);
    var h = padZero(Math.floor(s % 86400000 / 3600000), 2);
    var m = padZero(Math.floor(s % 3600000 / 60000), 2);
    var sec = padZero(Math.floor(s % 60000 / 1000), 2);
    return d + ':' + h + ':' + m + ':' + sec;
  };
};
