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

module.exports = function($scope, application) {
    $scope.application = application;
    $scope.metrics = {};

    application.getInfo().then(function(response) {
        $scope.info = response.data;
    }).catch(function(response) {
        $scope.error = response.data;
    });

    application.getHealth().then(function(response) {
        $scope.health = response.data;
    }).catch(function(response) {
        $scope.health = response.data;
    });

    application.getMetrics().then(
            function(response) {
                $scope.metrics = response.data;
                $scope.hasDatasources = false;
                $scope.hasCaches = false;
                angular.forEach($scope.metrics, function (value, key) {
                    if (!$scope.hasDatasources && key.startsWith('datasource.')) {
                        $scope.hasDatasources = true;
                    }
                    if (!$scope.hasCaches && key.startsWith('cache.')) {
                        $scope.hasCaches = true;
                    }
                });
            }).catch(function(response) {
                $scope.error = response.data;
            });
};
