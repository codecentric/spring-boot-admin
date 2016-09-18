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

var module = angular.module('sba-events', ['sba-core']);
global.sbaModules.push(module.name);

module.controller('eventsCtrl', function ($scope, $http) {
  $http.get('api/journal').then(function (response) {
    $scope.journal = response.data;
  }).catch(function (response) {
    $scope.error = response.data;
  });
});

module.config(function ($stateProvider) {
  $stateProvider.state('events', {
    name: 'events',
    url: '/events',
    templateUrl: 'events/events.html',
    controller: 'eventsCtrl'
  });
});

module.run(function (MainViews) {
  MainViews.register({
    title: 'Journal',
    state: 'events',
    order: 100
  });
});
