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
    value: '<value'
  },
  controller: function ($interval, $filter) {
    'ngInject';

    var ctrl = this;
    var timer = null;
    var start = 0;
    ctrl.$onChanges = function () {
      ctrl.clock = $filter('timeInterval')(ctrl.value);
      start = Date.now();

      if (timer !== null) {
        $interval.cancel(timer);
        timer = null;
      }

      timer = $interval(function () {
        ctrl.clock = $filter('timeInterval')(ctrl.value + Date.now() - start);
      }, 1000);
    };
  },
  template: '{{$ctrl.clock}} [d:h:m:s]'
};
