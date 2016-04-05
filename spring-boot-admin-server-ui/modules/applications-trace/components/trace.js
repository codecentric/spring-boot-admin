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
    trace: '<value'
  },
  controller: function () {
    var ctrl = this;
    ctrl.show = false;
    ctrl.statusClass = 'unknown';
    ctrl.toggle = function () {
      ctrl.show = !ctrl.show;
    };
    ctrl.$onChanges = function () {
      if (ctrl.trace.info.headers.response) {
        var status = parseInt(ctrl.trace.info.headers.response.status);
        if (Math.floor(status / 500) === 1) {
          ctrl.statusClass = 'error';
        } else if (Math.floor(status / 400) === 1) {
          ctrl.statusClass = 'warn';
        } else {
          ctrl.statusClass = 'ok';
        }
      }
    };
  },
  template: require('./trace.tpl.html')
};
