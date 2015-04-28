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

module.exports = function ($scope, application) {
    $scope.application = application;
    application.getActiviti()
        .success(function (activiti){
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
        })
        .error(function (error) {
            $scope.error = error;
        });
};
