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

require('es5-shim');
require('es5-sham');

require('jquery');
var angular = require('angular');
require('angular-resource');
require('angular-route');
require('angular-ui-router');
require('angular-ui-bootstrap');
require('angular-ui-bootstrap-tpls');
require('jolokia');

var springBootAdmin = angular.module('springBootAdmin', [
    'ngResource',
    'ngRoute',
    'ui.router',
    'ui.bootstrap',
]);

require('./controller');
require('./service');
require('./filter');
require('./directive');

springBootAdmin.config(function ($stateProvider, $urlRouterProvider) {
    $urlRouterProvider
        .when('/', '/overview')
        .otherwise('/');

    $stateProvider
        .state('overview', {
            url: '/overview',
            templateUrl: 'views/overview.html',
            controller: 'overviewCtrl'
        })
        .state('about', {
            url: '/about',
            templateUrl: 'views/about.html'
        })
        .state('apps', {
            abstract: true,
            url: '/apps/:id',
            controller: 'appsCtrl',
            templateUrl: 'views/apps.html',
            resolve: {
                application: function ($stateParams, Application) {
                    return Application.query({
                            id: $stateParams.id
                        })
                        .$promise;
                }
            }
        })
        .state('apps.details', {
            url: '/details',
            templateUrl: 'views/apps/details.html',
            controller: 'detailsCtrl'
        })
        .state('apps.details.metrics', {
            url: '/metrics',
            templateUrl: 'views/apps/details/metrics.html',
            controller: 'detailsMetricsCtrl'
        })
        .state('apps.details.env', {
            url: '/env',
            templateUrl: 'views/apps/details/env.html',
            controller: 'detailsEnvCtrl'
        })
        .state('apps.details.props', {
            url: '/props',
            templateUrl: 'views/apps/details/props.html',
            controller: 'detailsPropsCtrl'
        })
        .state('apps.details.classpath', {
            url: '/classpath',
            templateUrl: 'views/apps/details/classpath.html',
            controller: 'detailsClasspathCtrl'
        })
        .state('apps.logging', {
            url: '/logging',
            templateUrl: 'views/apps/logging.html',
            controller: 'loggingCtrl'
        })
        .state('apps.jmx', {
            url: '/jmx',
            templateUrl: 'views/apps/jmx.html',
            controller: 'jmxCtrl'
        })
        .state('apps.threads', {
            url: '/threads',
            templateUrl: 'views/apps/threads.html',
            controller: 'threadsCtrl'
        });
});
springBootAdmin.run(function ($rootScope, $state) {
    $rootScope.$state = $state;
});
