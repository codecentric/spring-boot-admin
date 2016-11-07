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

module.exports = function () {
  'ngInject';

  return function () {
    this.groups = [];

    var getMaxStatus = function (statusCounter) {
      var order = ['OFFLINE', 'DOWN', 'OUT_OF_SERVICE', 'UNKNOWN', 'UP'];
      for (var i = 0; i < order.length; i++) {
        if (statusCounter[order[i]] && statusCounter[order[i]] > 0) {
          return order[i];
        }
      }
      return 'UNKNOWN';
    };

    var findApplication = function (applications, application) {
      for (var i = 0; i < applications.length; i++) {
        if (applications[i].id === application.id) {
          return i;
        }
      }
      return -1;
    };

    var findGroup = function (groups, name) {
      for (var i = 0; i < groups.length; i++) {
        if (groups[i].name === name) {
          return i;
        }
      }
      return -1;
    };

    var updateStatus = function (group) {
      var statusCounter = {};
      group.applications.forEach(function (application) {
        statusCounter[application.statusInfo.status] = ++statusCounter[application.statusInfo.status] || 1;
      });
      group.statusCounter = statusCounter;
      group.status = getMaxStatus(statusCounter);
    };

    this.addApplication = function (application, overwrite) {
      var groupIdx = findGroup(this.groups, application.name);
      var group = groupIdx > -1 ? this.groups[groupIdx] : { applications: [], statusCounter: {}, versionsCounter: [], name: application.name, status: 'UNKNOWN' };
      if (groupIdx === -1) {
        this.groups.push(group);
      }
      var index = findApplication(group.applications, application);
      if (index === -1) {
        group.applications.push(application);
      } else if (overwrite) {
        group.applications[index] = application;
      }
      updateStatus(group);
      return group;
    };

    this.removeApplication = function (application) {
      var groupIdx = findGroup(this.groups, application.name);
      if (groupIdx > -1) {
        var group = this.groups[groupIdx];
        var index = findApplication(group.applications, application);
        if (index > -1) {
          group.applications.splice(index, 1);
          updateStatus(group);
        }
        if (group.length === 0) {
          this.groups.splice(groupIdx, 1);
        }
      }
    };
  };
};
