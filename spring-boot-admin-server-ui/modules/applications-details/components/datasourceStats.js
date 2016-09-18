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
  controller: function ($filter) {
    'ngInject';

    var ctrl = this;
    ctrl.$onChanges = function () {
      ctrl.datasources = [];
      angular.forEach(ctrl.metrics, function (value, key) {
        var match = /datasource\.(.+)\.active/.exec(key);
        if (match !== null) {
          ctrl.datasources.push({
            name: match[1],
            active: value,
            usage: $filter('number')(ctrl.metrics['datasource.' + match[1] + '.usage'] * 100, 2)
          });
        }
      });
    };
    ctrl.getBarClass = function (percentage) {
      if (percentage < 75) {
        return 'bar-success';
      } else if (percentage >= 75 && percentage <= 95) {
        return 'bar-warning';
      } else {
        return 'bar-danger';
      }
    };
  },
  template: require('./datasourceStats.tpl.html')
};
