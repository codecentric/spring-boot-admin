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
    statusInfo: '<statusInfo'
  },
  controller: function () {
    'ngInject';
    var ctrl = this;
    ctrl.showPopover = false;

    var isDetail = function (detail) {
      return detail !== null && !Array.isArray(detail) && typeof detail === 'object' && 'status' in detail;
    };

    ctrl.$onChanges = function () {
      ctrl.details = [];
      angular.forEach(ctrl.statusInfo.details, function (value, key) {
        if (isDetail(value)) {
          ctrl.details.push({ name: key, status: value.status });
        }
      });
    };
  },
  template: require('./statusInfo.tpl.html')
};
