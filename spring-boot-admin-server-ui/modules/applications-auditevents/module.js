/*
 * Copyright 2017 the original author or authors.
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

var module = angular.module('sba-applications-auditevents', ['sba-applications']);
global.sbaModules.push(module.name);

module.controller('auditeventsCtrl', require('./auditeventsCtrl.js'));
module.component('sbaAuditevent', require('./components/auditevent.js'));

module.config(function ($stateProvider) {
  $stateProvider.state('applications.auditevents', {
    url: '/auditevents',
    templateUrl: 'applications-auditevents/auditevents.html',
    controller: 'auditeventsCtrl'
  });
});

module.run(function (ApplicationViews, $sce, $http) {
  ApplicationViews.register({
    order: 55,
    title: $sce.trustAsHtml('<i class="fa fa-user-circle-o fa-fw"></i>Audit'),
    state: 'applications.auditevents',
    show: function (application) {
      return $http.head('api/applications/' + application.id + '/auditevents').then(function () {
        return true;
      }).catch(function () {
        return false;
      });
    }
  });
});
