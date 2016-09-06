/*
 * Copyright 2016 the original author or authors.
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

module.exports = function ($http) {
  'ngInject';

  var Logtail = function (url, initialSize) {
    this.url = url;
    this.initialSize = initialSize || (300 * 1024);
    this.logSize = 0;
  };

  Logtail.prototype.refresh = function () {
    var self = this;
    var initial = (self.logSize === 0);
    var range;
    if (initial) {
      range = '-' + self.initialSize.toString();
    } else {
      // Reload the last byte to avoid a 416: Range unsatisfiable.
      // If the response has length = 1 the file hasn't beent changed.
      // If the response status is 416 the logfile has been truncated.
      range = (self.logSize - 1).toString() + '-';
    }

    return $http.get(self.url, {
      headers: {
        'Range': 'bytes=' + range
      }
    }).then(function (response) {
      var contentSize = parseInt(response.headers('Content-Length'));
      if (response.status === 206) {
        self.logSize = parseInt(response.headers('Content-Range').split('/')[1]);
      } else if (response.status === 200) {
        if (!initial) {
          throw 'Expected 206 - Partial Content on subsequent requests.';
        }
        self.logSize = contentSize;
      } else {
        throw 'Unexpected resopnse status: ' + response.status;
      }

      var chunk = {
        totalSize: self.logSize,
        first: false,
        addendum: null
      };

      if (initial) {
        chunk.first = true;
        if (contentSize < self.logSize) {
          // In case of a partial response find the first line break.
          chunk.addendum = response.data.substring(response.data.indexOf('\n') + 1);
        } else {
          chunk.addendum = response.data;
        }
      } else if (response.data.length > 1) {
        // Remove the first byte which has been part of the previos response.
        chunk.addendum = response.data.substring(1);
      }

      return chunk;
    });
  };

  return Logtail;
};
