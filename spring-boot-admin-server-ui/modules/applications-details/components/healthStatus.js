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

require('./healthStatus.css');

module.exports = {
  bindings: {
    health: '<health'
  },
  controller: function ($scope) {
    'ngInject';

    var ctrl = this;
    ctrl.isHealthDetail = function (key, value) {
      return key !== 'status' && value !== null && (Array.isArray(value) || typeof value !== 'object');
    };
    ctrl.isChildHealth = function (key, health) {
      return health !== null && !Array.isArray(health) && typeof health === 'object';
    };
    ctrl.$onChanges = function () {
      $scope.health = ctrl.health;
      $scope.name = 'application';
    };
  },
  template: '<ng-include src="\'applications-details/views/templates/health-status.html\'"></ng-include>'
};
