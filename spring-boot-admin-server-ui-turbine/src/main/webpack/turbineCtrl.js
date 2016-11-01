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

module.exports = function ($scope, $http) {
  'ngInject';
  $scope.error = null;
  $scope.hystrixStream = null;
  $scope.selectedCluster = 'default';
  $scope.clusters = ['default'];

  $http.get('api/turbine/clusters').then(function (response) {
    if (response.data.clusters.length > 0) {
      $scope.clusters = response.data.clusters;
    }
    $scope.selectedCluster = $scope.clusters[0];
    $scope.startStream();
  });

  $scope.stopStream = function () {
    if ($scope.hystrixStream !== null) {
      $scope.hystrixStream.close();
      $scope.hystrixStream = null;
    }
  };

  $scope.startStream = function () {
    $scope.stopStream();
    $scope.streamUrl = 'api/turbine/stream?cluster=' + $scope.selectedCluster;

    $scope.hystrixStream = new EventSource($scope.streamUrl);
    $scope.hystrixStream.addEventListener('message', function () {
      if ($scope.error) {
        $scope.error = null;
        $scope.$apply();
      }
    }, false);
    $scope.hystrixStream.addEventListener('error', function (e) {
      $scope.error = e;
      $scope.$apply();
    }, false);
  };

  $scope.$on('$destroy', function () {
    $scope.stopStream();
  });

};
