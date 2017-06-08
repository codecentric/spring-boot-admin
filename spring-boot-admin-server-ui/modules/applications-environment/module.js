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

var module = angular.module('sba-applications-environment', ['sba-applications']);
global.sbaModules.push(module.name);

module.controller('environmentCtrl', require('./controllers/environmentCtrl.js'));

module.component('sbaEnvironmentManager', require('./components/environmentManager.js'));

module.config(function ($stateProvider) {
  $stateProvider.state('applications.environment', {
    url: '/environment',
    templateUrl: 'applications-environment/views/environment.html',
    controller: 'environmentCtrl'
  });
});

module.run(function (ApplicationViews, $http, $sce) {
  ApplicationViews.register({
    order: 10,
    title: $sce.trustAsHtml('<i class="fa fa-server fa-fw"></i>Environment'),
    state: 'applications.environment',
    show: function (application) {
      return $http.head('api/applications/' + application.id + '/env').then(function () {
        return true;
      }).catch(function () {
        return false;
      });
    }
  });
});
