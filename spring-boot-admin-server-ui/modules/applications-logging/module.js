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
var module = angular.module('sba-applications-logging', ['sba-applications-jmx']);
global.sbaModules.push(module.name);

module.service('ApplicationLogging', require('./services/applicationLogging.js'));

module.controller('loggingCtrl', require('./controllers/loggingCtrl.js'));

module.component('sbaLogger', require('./components/logger.js'));

module.config(function ($stateProvider) {
  $stateProvider.state('applications.logging', {
    url: '/logging',
    templateUrl: 'applications-logging/views/logging.html',
    controller: 'loggingCtrl'
  });
});

module.run(function (ApplicationViews, $sce, $http) {
  ApplicationViews.register({
    order: 30,
    title: $sce.trustAsHtml('<i class="fa fa-sliders fa-fw"></i>Logging'),
    state: 'applications.logging',
    show: function (application) {
      return $http.head('api/applications/' + application.id + '/loggers').then(function () {
        return true;
      }).catch(function () {
        return $http.head('api/applications/' + application.id + '/jolokia').then(function () {
          return true;
        }).catch(function () {
          return false;
        });
      });
    }
  });
});
