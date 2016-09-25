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


require('hystrix/hystrixThreadPool.css');

var module = angular.module('sba-applications-hystrix', ['sba-applications']);
global.sbaModules.push(module.name);

module.component('sbaHystrixCommand', require('./components/hystrixCommand.js'));
module.component('sbaHystrixThreadPool', require('./components/hystrixThreadPool.js'));

module.controller('hystrixCtrl', require('./controllers/hystrixCtrl.js')('hystrix.stream'));
module.controller('turbineCtrl', require('./controllers/hystrixCtrl.js')('turbine.stream'));

module.config(function ($stateProvider) {
  $stateProvider.state('applications.hystrix', {
    url: '/hystrix',
    templateUrl: 'applications-hystrix/views/hystrix.html',
    controller: 'hystrixCtrl'
  });
  $stateProvider.state('applications.turbine', {
    url: '/turbine',
    templateUrl: 'applications-hystrix/views/hystrix.html',
    controller: 'turbineCtrl'
  });
});


module.run(function (ApplicationViews, $sce, $q, $http) {
  ApplicationViews.register({
    order: 150,
    title: $sce.trustAsHtml('<i class="fa fa-gear fa-fw"></i>Hystrix'),
    state: 'applications.hystrix',
    show: function (application) {
      if (!application.managementUrl || !application.statusInfo.status || application.statusInfo.status === 'OFFLINE') {
        return false;
      }
      return $http.head('api/applications/' + application.id + '/hystrix.stream').then(function () {
        return true;
      }).catch(function () {
        return false;
      });
    }
  });
  ApplicationViews.register({
    order: 155,
    title: $sce.trustAsHtml('<i class="fa fa-gear fa-fw"></i>Turbine'),
    state: 'applications.turbine',
    show: function (application) {
      if (!application.managementUrl || !application.statusInfo.status || application.statusInfo.status === 'OFFLINE') {
        return false;
      }
      return $http.head('api/applications/' + application.id + '/turbine.stream').then(function () {
        return true;
      }).catch(function () {
        return false;
      });
    }
  });
});
