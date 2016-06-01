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
module.exports = {
  bindings: {
    application: '<application',
    filters: '<filters',
    refreshFilters: '&refreshCallback'
  },
  controller: function (NotificationFilters) {
    'ngInject';
    var ctrl = this;

    ctrl.$onInit = function () {
      ctrl.ttl = '-1';
      ctrl.filterType = 'by-name';
    };

    ctrl.$onChanges = function () {
      ctrl.activeFilters = NotificationFilters.getActiveFilters(ctrl.filters, ctrl.application);
      ctrl.hasActiveFilter = !angular.equals({}, ctrl.activeFilters);
      if (!ctrl.hasActiveFilter) {
        ctrl.showPopover = false;
      }
    };

    ctrl.removeFilter = function (filterId) {
      return NotificationFilters.removeFilter(filterId).then(function () {
        ctrl.refreshFilters();
      });
    };

    ctrl.addFilter = function () {
      var ttl = parseInt(ctrl.ttl);
      if (ttl >= 0) {
        ttl = ttl * 60000;
      }

      var promise = null;
      switch (ctrl.filterType) {
        case 'by-name':
          promise = NotificationFilters.addFilterByName(ctrl.application.name, ttl);
          break;
        case 'by-id':
          promise = NotificationFilters.addFilterById(ctrl.application.id, ttl);
          break;
        default:
          return;
      }

      return promise.then(function () {
        ctrl.showPopover = false;
        ctrl.refreshFilters();
      });
    };
  },
  template: require('./notificationSettings.tpl.html')
};