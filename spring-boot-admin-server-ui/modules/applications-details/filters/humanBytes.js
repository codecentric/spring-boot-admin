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
  var units = {
    B: Math.pow(1024, 0),
    K: Math.pow(1024, 1),
    M: Math.pow(1024, 2),
    G: Math.pow(1024, 3),
    T: Math.pow(1024, 4),
    P: Math.pow(1024, 5)
  };

  return function (input, unit) {
    input = input || 0;
    unit = unit || 'B';

    var bytes = input * (units[unit] || 1);

    var chosen = 'B';
    for (var u in units) {
      if (units[chosen] < units[u] && bytes >= units[u]) {
        chosen = u;
      }
    }

    return (bytes / units[chosen]).toFixed(1).replace(/\.0$/, '').replace(/,/g, '') + chosen;
  };
};
