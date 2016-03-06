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

var module = angular.module('sba-applications-threads', [ 'sba-applications' ]);
global.sbaModules.push(module.name);

module.controller('threadsCtrl', require('./controllers/threadsCtrl'));

module.component('sbaThread', require('./components/thread'));
module.component('sbaThreadSummary', require('./components/threadSummary'));

module.config(function($stateProvider) {
    $stateProvider.state('applications.threads', {
        url : '/threads',
        templateUrl : 'applications-threads/views/threads.html',
        controller : 'threadsCtrl'
    });
});

module.run(function(ApplicationViews) {
    ApplicationViews.register({
        order : 50,
        title : 'Threads',
        state : 'applications.threads',
        show : function (application) {
            return application.managementUrl && application.statusInfo.status !== null && application.statusInfo.status !== 'OFFLINE';
        }
    });
});
