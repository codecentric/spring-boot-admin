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

var linkify = require('linkifyjs/string');

module.exports = function () {
  return function (input, maxLength) {
    var max = maxLength || Number.MAX_VALUE;
    return linkify(input, {
      target: '_blank',
      format: function(url, type) {
        if (type === 'url' && url.length > max) {
          url = url.replace(/https?:\/\//, '');
          if (url.length > max) {
            var last = url.lastIndexOf('/');
            if (last >= 0 && (url.length - last) < max) {
              var end = url.substr(last);
              var first = url.lastIndexOf('/', max - 3 - end.length);
              if (first < 0) {
                first = max - 3 - end.length;
              }
              url = url.substr(0, first + 1) + '...' + end;
            } else {
              url = url.substr(0, max - 3) + '...';
            }
          }
        }
        return url;
      }
    });
  };
};
