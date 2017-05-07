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

var module = angular.module('sba-applications-jmx', ['sba-applications']);
global.sbaModules.push(module.name);

module.service('ApplicationJmx', require('./services/applicationJmx.js'));
module.service('jolokia', require('./services/jolokia.js'));

module.controller('jmxCtrl', require('./controllers/jmxCtrl.js'));

module.component('sbaJmxBean', require('./components/jmxBean.js'));
module.component('sbaJavaTypeInput', require('./components/javaTypeInput.js'));
module.component('sbaJmxInvocation', require('./components/jmxInvocation.js'));
module.component('sbaJmxInvokeSelectOverload', require('./components/jmxInvokeSelectOverload.js'));
module.component('sbaJmxInvokeInputParameters', require('./components/jmxInvokeInputParameters.js'));
module.component('sbaJmxInvokeShowResult', require('./components/jmxInvokeShowResult.js'));

module.config(function ($stateProvider) {
  $stateProvider.state('applications.jmx', {
    url: '/jmx',
    templateUrl: 'applications-jmx/views/jmx.html',
    controller: 'jmxCtrl'
  });
});

module.run(function (ApplicationViews, $sce, $http, jolokia, ApplicationJmx) {
  ApplicationViews.register({
    order: 40,
    title: $sce.trustAsHtml('<i class="fa fa-cogs fa-fw"></i>JMX'),
    state: 'applications.jmx',
    show: function (application) {
      return $http.head('api/applications/' + application.id + '/jolokia').then(function () {
        return ApplicationJmx.list(application).then(function () {
          return true;
        }).catch(function () {
          return false;
        });
      }).catch(function () {
        return false;
      });
    }
  });
});
