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

var module = angular.module('sba-applications-details', [ 'sba-applications' ]);
global.sbaModules.push(module.name);

module.controller('detailsCtrl', require('./controllers/detailsCtrl'));

module.filter('humanBytes', require('./filters/humanBytes'));
module.filter('joinArray', require('./filters/joinArray'));
module.filter('timeInterval', require('./filters/timeInterval'));

module.component('sbaHealthStatus', require('./components/healthStatus'));
module.component('sbaUptime', require('./components/uptime'));
module.component('sbaMemoryStats', require('./components/memoryStats'));
module.component('sbaJvmStats', require('./components/jvmStats'));
module.component('sbaGcStats', require('./components/gcStats'));
module.component('sbaServletContainerStats', require('./components/servletContainerStats'));
module.component('sbaDatasourceStats', require('./components/datasourceStats'));
module.component('sbaCacheStats', require('./components/cacheStats'));

module.config(function($stateProvider) {
    $stateProvider.state('applications.details', {
        url : '/details',
        templateUrl : 'applications-details/views/details.html',
        controller : 'detailsCtrl'
    });
});

module.run(function(ApplicationViews) {
    ApplicationViews.register({
        order : 0,
        title : 'Details',
        state : 'applications.details',
        show : function (application) {
            return application.managementUrl && application.statusInfo.status !== null && application.statusInfo.status !== 'OFFLINE';
        }
    });
});
