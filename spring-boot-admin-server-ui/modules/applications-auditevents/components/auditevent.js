/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, softwareÍ
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

require('./auditevent.css');

module.exports = {
  bindings: {
    auditevent: '<value',
    filterCallback: '&filterCallback'
  },
  controller: function () {
    var ctrl = this;
    ctrl.show = false;
    ctrl.toggle = function () {
      ctrl.show = !ctrl.show;
    };
    ctrl.filterPrincipal = function (event) {
      ctrl.filterCallback()('principal', ctrl.auditevent.principal);
      event.stopPropagation();
    };
    ctrl.filterType = function (event) {
      ctrl.filterCallback()('type', ctrl.auditevent.type);
      event.stopPropagation();
    };
    ctrl.getStatusClass = function () {
      if (/success/i.test(ctrl.auditevent.type)) {
        return 'success';
      } if (/failure/i.test(ctrl.auditevent.type) || /denied/i.test(ctrl.auditevent.type)) {
        return 'failure';
      } else {
        return 'unknown';
      }
    };
  },
  template: require('./auditevent.tpl.html')
};
