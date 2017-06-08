/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

module.exports = function ($scope, $http, application) {
  'ngInject';
  var halfAnHourBefore = new Date();
  halfAnHourBefore.setMinutes(halfAnHourBefore.getMinutes() - 30);
  halfAnHourBefore.setSeconds(0);
  halfAnHourBefore.setMilliseconds(0);

  $scope.auditevents = [];
  $scope.filter = {principal: null, type: null, after: halfAnHourBefore};

  $scope.setFilter = function (key, value) {
    $scope.filter[key] = value;
    $scope.search();
  };

  var trimToNull = function (s) {
    if (s !== null) {
      s = s.trim();
      if (s === '') {
        s = null;
      }
    }
    return s;
  };

  var convert = function (d) {
    if (d === null) {
      return d;
    }
    return d.toISOString().replace(/Z$/, '+0000').replace(/\.\d{3}\+/, '+');
  };

  $scope.search = function () {
    $http({
      url: 'api/applications/' + application.id + '/auditevents',
      method: 'GET',
      params: {
        principal: trimToNull($scope.filter.principal),
        type: trimToNull($scope.filter.type),
        after: convert($scope.filter.after)
      }
    }).then(function (response) {
      $scope.auditevents = response.data.events;
      $scope.error = null;
    }).catch(function (response) {
      $scope.error = response.data;
    });
  };

  $scope.search();
};
