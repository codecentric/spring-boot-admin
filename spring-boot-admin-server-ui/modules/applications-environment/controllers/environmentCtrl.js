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

  $scope.application = application;
  $scope.refreshSupported = false;

  $http.head('api/applications/' + application.id + '/refresh').catch(function (response) {
    $scope.refreshSupported = response.status === 405; //If method not allowed is returned the endpoint is present.
  });

  var toArray = function (obj) {
    return Object.getOwnPropertyNames(obj).map(function (key) {
      var value = obj[key] instanceof Object ? toArray(obj[key]) : obj[key];
      return {
        name: key,
        value: value
      };
    });
  };

  $scope.refresh = function () {
    $scope.env = [];
    $scope.profiles = [];

    application.getEnv().then(function (response) {
      var env = response.data;
      $scope.profiles = env.profiles;
      delete env.profiles;
      $scope.env = toArray(env); // to get the env-sources in correct order we have to convert to an array
    }).catch(function (response) {
      $scope.error = response.data;
    });
  };

  $scope.refresh();
};
