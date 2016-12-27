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

module.exports = function ($scope, $http, application) {
  'ngInject';

  $scope.reports = [];
  $scope.successStates = ['BASELINE', 'MISSING_SUCCESS', 'SUCCESS', 'OUT_OF_ORDER', 'FUTURE_SUCCESS'];
  $scope.warningStates = ['PENDING', 'ABOVE_TARGET', 'PREINIT', 'BELOW_BASELINE', 'IGNORED'];
  $scope.failedStates = ['MISSING_FAILED', 'FAILED', 'FUTURE_FAILED'];
  $scope.searchFilter;

  $scope.refresh = function () {
    $http.get('api/applications/' + application.id + '/flyway').then(function (response) {
      if (Array.isArray(response.data) && (response.data.length === 0 || response.data[0].hasOwnProperty('name'))) {
        $scope.reports = response.data;
      } else {
        $scope.reports = [{ name: 'flyway', migrations: response.data }];
      }
    });
  };
  $scope.refresh();

  $scope.inArray = function (migrationStatus, statusArray) {
    return statusArray.indexOf(migrationStatus) !== -1;
  };
};
