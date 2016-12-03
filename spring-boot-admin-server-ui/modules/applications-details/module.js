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

var module = angular.module('sba-applications-details', ['sba-applications']);
global.sbaModules.push(module.name);

module.controller('detailsCtrl', require('./controllers/detailsCtrl.js'));

module.filter('humanBytes', require('./filters/humanBytes.js'));
module.filter('joinArray', require('./filters/joinArray.js'));
module.filter('timeInterval', require('./filters/timeInterval.js'));

module.component('sbaHealthStatus', require('./components/healthStatus.js'));
module.component('sbaUptime', require('./components/uptime.js'));
module.component('sbaMemoryStats', require('./components/memoryStats.js'));
module.component('sbaJvmStats', require('./components/jvmStats.js'));
module.component('sbaGcStats', require('./components/gcStats.js'));
module.component('sbaServletContainerStats', require('./components/servletContainerStats.js'));
module.component('sbaDatasourceStats', require('./components/datasourceStats.js'));
module.component('sbaCacheStats', require('./components/cacheStats.js'));

module.config(function ($stateProvider) {
  $stateProvider.state('applications.details', {
    url: '/details',
    templateUrl: 'applications-details/views/details.html',
    controller: 'detailsCtrl'
  });
});

module.run(function (ApplicationViews, $sce) {
  ApplicationViews.register({
    order: 0,
    title: $sce.trustAsHtml('<i class="fa fa-info fa-fw"></i>Details'),
    state: 'applications.details'
  });
});
