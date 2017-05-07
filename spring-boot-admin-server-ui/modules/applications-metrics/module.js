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

var module = angular.module('sba-applications-metrics', ['sba-applications']);
global.sbaModules.push(module.name);

module.controller('metricsCtrl', require('./controllers/metricsCtrl.js'));

module.directive('sbaSimpleMetricBar', require('./directives/simpleMetricBar.js'));
module.directive('sbaRichMetricBar', require('./directives/richMetricBar.js'));

module.config(function ($stateProvider) {
  $stateProvider.state('applications.metrics', {
    url: '/metrics',
    templateUrl: 'applications-metrics/views/metrics.html',
    controller: 'metricsCtrl'
  });
});

module.run(function (ApplicationViews, $http, $sce) {
  ApplicationViews.register({
    order: 5,
    title: $sce.trustAsHtml('<i class="fa fa-bar-chart fa-fw"></i>Metrics'),
    state: 'applications.metrics',
    show: function (application) {
      return $http.head('api/applications/' + application.id + '/metrics').then(function () {
        return true;
      }).catch(function () {
        return false;
      });
    }
  });
});
