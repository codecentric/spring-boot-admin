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

var module = angular.module('sba-applications-flyway', ['sba-applications']);
global.sbaModules.push(module.name);

module.controller('flywayCtrl', require('./controllers/flywayCtrl.js'));

module.config(function ($stateProvider) {
  $stateProvider.state('applications.flyway', {
    url: '/flyway',
    templateUrl: 'applications-flyway/views/flyway.html',
    controller: 'flywayCtrl'
  });
});

module.run(function (ApplicationViews, $http, $sce) {
  ApplicationViews.register({
    order: 100,
    title: $sce.trustAsHtml('<i class="fa fa-database fa-fw"></i>Flyway'),
    state: 'applications.flyway',
    show: function (application) {
      return $http.head('api/applications/' + application.id + '/flyway').then(function () {
        return true;
      }).catch(function () {
        return false;
      });
    }
  });
});
