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
    threads: '<threads'
  },
  controller: function ($filter) {
    'ngInject';

    var ctrl = this;
    ctrl.$onChanges = function () {
      ctrl.threads = ctrl.threads || [];

      ctrl.threadSummary = {
        NEW: {
          count: 0,
          percentage: 0,
          cssClass: 'bar-info'
        },
        RUNNABLE: {
          count: 0,
          percentage: 0,
          cssClass: 'bar-success'
        },
        BLOCKED: {
          count: 0,
          percentage: 0,
          cssClass: 'bar-danger'
        },
        WAITING: {
          count: 0,
          percentage: 0,
          cssClass: 'bar-warning'
        },
        TIMED_WAITING: {
          count: 0,
          percentage: 0,
          cssClass: 'bar-warning'
        },
        TERMINATED: {
          count: 0,
          percentage: 0,
          cssClass: 'bar-info'
        }
      };

      ctrl.threads.forEach(function (thread) {
        ctrl.threadSummary[thread.threadState].count++;
      });

      angular.forEach(ctrl.threadSummary, function (value) {
        value.percentage = $filter('number')(value.count / ctrl.threads.length * 100, 2);
      });
    };
  },
  template: require('./threadSummary.tpl.html')
};
