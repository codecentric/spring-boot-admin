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

(function (sbaModules, angular) {
  'use strict';
  var module = angular.module('sba-applications-activiti', ['sba-applications']);
  sbaModules.push(module.name);

  module.controller('activitiCtrl', ['$scope', '$http', 'application', function ($scope, $http, application) {
    $scope.application = application;
    $http.get('api/applications/' + application.id + '/activiti').then(function (response) {
      var activiti = response.data;
      $scope.summary = [];
      $scope.summary.push({
        key: 'Completed Task Count Today',
        value: activiti.completedTaskCountToday
      });
      $scope.summary.push({
        key: 'Process Definition Count',
        value: activiti.processDefinitionCount
      });
      $scope.summary.push({
        key: 'Cached Process Definition Count',
        value: activiti.cachedProcessDefinitionCount
      });
      $scope.summary.push({
        key: 'Completed Task Count',
        value: activiti.completedTaskCount
      });
      $scope.summary.push({
        key: 'Completed Activities',
        value: activiti.completedActivities
      });
      $scope.summary.push({
        key: 'Open Task Count',
        value: activiti.openTaskCount
      });
      $scope.processes = [];
      for (var i = 0; i < activiti.deployedProcessDefinitions.length; i++) {
        var process = activiti.deployedProcessDefinitions[i];
        var runningProcessInstanceCount = activiti.runningProcessInstanceCount[process];
        var completedProcessInstanceCount = activiti.completedProcessInstanceCount[process];
        $scope.processes.push({
          name: process,
          running: runningProcessInstanceCount,
          completed: completedProcessInstanceCount
        });
      }
    }).catch(function (response) {
      $scope.error = response.data;
    });
  }]);

  module.config(['$stateProvider', function ($stateProvider) {
    $stateProvider.state('applications.activiti', {
      url: '/activiti',
      templateUrl: 'applications-activiti/activiti.html',
      controller: 'activitiCtrl'
    });
  }]);

  module.run(['ApplicationViews', '$http', function (ApplicationViews, $http) {
    ApplicationViews.register({
      order: 100,
      title: 'Activiti',
      state: 'applications.activiti',
      show: function (application) {
        return $http.get('api/applications/' + application.id + '/configprops').then(
          function (response) {
            return response.data.processEngineEndpoint !== undefined;
          }).catch(function () {
            return false;
          });
      }
    });
  }]);
} (sbaModules, angular));
