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
    'ui.bootstrap'
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
                    return Application.get({
                        id: $stateParams.id
                    }).$promise;
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
        .state('apps.details.classpath', {
            url: '/classpath',
            templateUrl: 'views/apps/details/classpath.html',
            controller: 'detailsClasspathCtrl'
        })
        .state('apps.env', {
            url: '/env',
            templateUrl: 'views/apps/environment.html',
            controller: 'environmentCtrl'
        })
        .state('apps.activiti', {
            url: '/activiti',
            templateUrl: 'views/apps/activiti.html',
            controller: 'activitiCtrl'
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
        })
        .state('apps.trace', {
            url: '/trace',
            templateUrl: 'views/apps/trace.html',
            controller: 'traceCtrl'
        })
       .state('journal', {
            url: '/journal',
            templateUrl: 'views/journal.html',
            controller: 'journalCtrl'
        });
});

springBootAdmin.run(function ($rootScope, $state, $log, $filter, Notification, Application ) {
    $rootScope.$state = $state;
    $rootScope.applications = [];
    $rootScope.indexOfApplication = function (id) {
       for (var i = 0; i <  $rootScope.applications.length; i++) {
            if ($rootScope.applications[i].id === id) {
                return i;
            }
       }
       return -1;
    };

    var refresh = function(application) {
        application.info = {};
        application.refreshing = true;
        application.getCapabilities();
        application.getInfo().then(function(info) {
            application.version = info.version;
            application.infoDetails = null;
            application.infoShort = '';
            delete info.version;
            var infoYml = $filter('yaml')(info);
            if (infoYml !== '{}\n') {
                application.infoShort = $filter('limitLines')(infoYml, 3);
                if (application.infoShort !== infoYml) {
                     application.infoDetails = $filter('limitLines')(infoYml, 32000, 3);
                }
            }
          }).finally(function(){
              application.refreshing = false;
          });
    };

    Application.query(function (applications) {
        for (var i = 0; i < applications.length; i++) {
                refresh(applications[i]);
        }
        $rootScope.applications = applications;
    });


    //setups up the sse-reciever
    var journalEventSource = new EventSource('api/journal');
    journalEventSource.onmessage = function(message) {
        var event = JSON.parse(message.data);
        Object.setPrototypeOf(event.application, Application.prototype);

        var options = { tag: event.application.id,
                        body: 'Instance ' + event.application.id + '\n' + event.application.healthUrl,
                        icon: 'img/unknown.png',
                        timeout: 10000,
                        url: $state.href('apps.details', {id: event.application.id}) };
        var title = event.application.name;
        var index = $rootScope.indexOfApplication(event.application.id);

        if (event.type === 'REGISTRATION') {
            if (index === -1) {
                $rootScope.applications.push(event.application);
            }

            title += ' instance registered.';
            options.tag = event.application.id + '-REGISTRY';
        } else if (event.type === 'DEREGISTRATION') {
            if (index > -1) {
                $rootScope.applications.splice(index, 1);
            }

            title += ' instance removed.';
            options.tag = event.application.id + '-REGISTRY';
        } else if (event.type === 'STATUS_CHANGE') {
            refresh(event.application);
            if (index > -1) {
                $rootScope.applications[index] = event.application;
            } else {
                $rootScope.applications.push(event.application);
            }

            title += ' instance is ' + event.to.status;
            options.tag = event.application.id + '-STATUS';
            options.icon = event.to.status !== 'UP' ? 'img/error.png' : 'img/ok.png';
            options.body = event.from.status + ' --> ' + event.to.status + '\n' + options.body;
        }

        $rootScope.$apply();
        Notification.notify(title, options);
    };

    journalEventSource.onerror = function(event) {
        $log.error('Could not read server sent event!', event);
    };
});
