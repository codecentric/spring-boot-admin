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

module.exports = {
  bindings: {
    metrics: '<metrics'
  },
  controller: function ($filter) {
    'ngInject';

    var ctrl = this;
    ctrl.$onChanges = function () {
      ctrl.memory = {
        total: ctrl.metrics.mem,
        used: ctrl.metrics.mem - ctrl.metrics['mem.free'],
        unit: 'K'
      };
      ctrl.memory.percentUsed = $filter('number')(ctrl.memory.used / ctrl.memory.total * 100, 2);

      ctrl.heap = {
        total: ctrl.metrics['heap.committed'],
        used: ctrl.metrics['heap.used'],
        init: ctrl.metrics['heap.init'],
        max: ctrl.metrics['heap.max'] || ctrl.metrics.heap,
        unit: ctrl.metrics['heap.max'] ? 'B' : 'K'
      };
      ctrl.heap.percentUsed = $filter('number')(ctrl.heap.used / ctrl.heap.total * 100, 2);

      ctrl.nonheap = {
        total: ctrl.metrics['nonheap.committed'],
        used: ctrl.metrics['nonheap.used'],
        init: ctrl.metrics['nonheap.init'],
        max: ctrl.metrics.nonheap,
        unit: 'K'
      };
      ctrl.nonheap.percentUsed = $filter('number')(ctrl.nonheap.used / ctrl.nonheap.total * 100, 2);
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
  template: require('./memoryStats.tpl.html')
};
