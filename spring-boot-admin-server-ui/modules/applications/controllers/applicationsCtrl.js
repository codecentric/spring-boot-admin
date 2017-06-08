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
module.exports = function ($rootScope, $scope, $state, NotificationFilters, $filter) {
  'ngInject';
  $scope.notificationFilters = null;
  $scope.notificationFiltersSupported = false;
  $scope.expandAll = false;
  $scope.searchFilter = '';

  $scope.remove = function (application) {
    application.$remove();
  };

  $scope.toggleExpandAll = function () {
    $scope.expandAll = !$scope.expandAll;
    angular.forEach($rootScope.applicationGroups.groups, function (group) {
      group.collapsed = !$scope.expandAll;
    });
  };

  $scope.order = {
    column: 'name',
    descending: false
  };

  $scope.orderBy = function (column) {
    if (column === $scope.order.column) {
      $scope.order.descending = !$scope.order.descending;
    } else {
      $scope.order.column = column;
      $scope.order.descending = false;
    }
  };

  $scope.orderByCssClass = function (column) {
    if (column === $scope.order.column) {
      return 'sorted-' + ($scope.order.descending ? 'descending' : 'ascending');
    } else {
      return '';
    }
  };

  $scope.loadFilters = function () {
    return NotificationFilters.getFilters().then(function (filters) {
      $scope.notificationFilters = filters;
    });
  };
  $scope.loadFilters();

  $scope.doFilter = function (value) {
    if (!$scope.searchFilter) {
      return true;
    }
    var projection = angular.copy(value);
    delete projection.statusInfo;
    delete projection.group;
    return $filter('filter')([projection], $scope.searchFilter).length > 0;
  };
};
