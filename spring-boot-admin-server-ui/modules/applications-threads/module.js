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

var module = angular.module('sba-applications-threads', ['sba-applications']);
global.sbaModules.push(module.name);

module.controller('threadsCtrl', require('./controllers/threadsCtrl.js'));

module.component('sbaThread', require('./components/thread.js'));
module.component('sbaThreadSummary', require('./components/threadSummary.js'));

module.config(function ($stateProvider) {
  $stateProvider.state('applications.threads', {
    url: '/threads',
    templateUrl: 'applications-threads/views/threads.html',
    controller: 'threadsCtrl'
  });
});

module.run(function (ApplicationViews, $http, $sce) {
  ApplicationViews.register({
    order: 50,
    title: $sce.trustAsHtml('<i class="fa fa-list fa-fw"></i>Threads'),
    state: 'applications.threads',
    show: function (application) {
      return $http.head('api/applications/' + application.id + '/dump').then(function () {
        return true;
      }).catch(function () {
        return false;
      });
    }
  });
});
