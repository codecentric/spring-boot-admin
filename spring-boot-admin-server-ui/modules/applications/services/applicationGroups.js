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
    this.groups = {};
    this.applications = [];

    var getMaxStatus = function (statusCounter) {
      var order = ['OFFLINE', 'DOWN', 'OUT_OF_SERVICE', 'UNKNOWN', 'UP'];
      for (var i = 0; i < order.length; i++) {
        if (statusCounter[order[i]] && statusCounter[order[i]] > 0) {
          return order[i];
        }
      }
      return 'UNKNOWN';
    };

    this.updateStatus = function (group) {
      var statusCounter = {};
      var applicationCount = 0;
      this.applications.forEach(function (application) {
        if (application.name === group.name) {
          applicationCount++;
          statusCounter[application.statusInfo.status] = ++statusCounter[application.statusInfo.status] || 1;
        }
      });
      group.applicationCount = applicationCount;
      group.statusCounter = statusCounter;
      group.status = getMaxStatus(statusCounter);
    };

    this.addApplication = function (application, overwrite) {
      var idx = this.applications.findIndex(function (app) {
        return app.id === application.id;
      });
      this.groups[application.name] = this.groups[application.name] || { applicationCount: 0, statusCounter: {}, versionsCounter: [], name: application.name, status: 'UNKNOWN' };
      application.group = this.groups[application.name];
      if (idx < 0) {
        this.applications.push(application);
      } else if (overwrite) {
        this.applications.splice(idx, 1, application);
      }
      this.updateStatus(this.groups[application.name]);
    };

    this.removeApplication = function (id) {
      var idx = this.applications.findIndex(function (application) {
        return id === application.id;
      });
      var group = this.applications[idx].group;
      this.applications.splice(idx, 1);
      this.updateStatus(group);
    };
  };
};
