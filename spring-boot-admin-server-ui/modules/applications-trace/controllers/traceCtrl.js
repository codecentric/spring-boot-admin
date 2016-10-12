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

module.exports = function ($scope, application, $interval) {
  'ngInject';

  $scope.traces = [];
  $scope.refreshInterval = 3;
  $scope.refresher = null;

  $scope.refresh = function () {
    application.getTraces().then(function (response) {
      var oldestTrace = response.data[response.data.length - 1];
      var olderTraces = $scope.traces.filter(function (trace) {
        return trace.timestamp < oldestTrace.timestamp;
      });
      $scope.traces = response.data.concat(olderTraces);
    }).catch(function (response) {
      $scope.error = response.data;
    });
  };

  $scope.start = function () {
    $scope.refresher = $interval(function () {
      $scope.refresh();
    }, $scope.refreshInterval * 1000);
  };

  $scope.stop = function () {
    if ($scope.refresher !== null) {
      $interval.cancel($scope.refresher);
      $scope.refresher = null;
    }
  };

  $scope.toggleAutoRefresh = function () {
    if ($scope.refresher === null) {
      $scope.start();
    } else {
      $scope.stop();
    }
  };

  $scope.$on('$destroy', function () {
    $scope.stop();
  });

  $scope.refresh();
};
