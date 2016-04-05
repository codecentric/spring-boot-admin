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

var angular = require('angular');
module.exports = {
  bindings: {
    metrics: '<metrics'
  },
  controller: function () {
    var ctrl = this;
    ctrl.$onChanges = function () {
      ctrl.gcs = [];
      angular.forEach(ctrl.metrics, function (value, key) {
        var match = /gc\.(.+)\.time/.exec(key);
        if (match !== null) {
          ctrl.gcs.push({
            name: match[1],
            time: value + ' ms',
            count: ctrl.metrics['gc.' + match[1] + '.count']
          });
        }
      });
    };
  },
  template: require('./gcStats.tpl.html')
};
