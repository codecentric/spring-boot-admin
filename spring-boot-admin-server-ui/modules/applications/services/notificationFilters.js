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
module.exports = function ($http) {
  'ngInject';

  this.getFilters = function () {
    return $http.get('api/notifications/filters').then(function (response) {
      return response.data;
    }).catch(function () {
      return null;
    });
  };

  this.getActiveFilters = function (filters, application) {
    var appFilters = {};
    angular.forEach(filters, function (value, key) {
      if (!value.expired && (value.id === application.id || value.name === application.name)) {
        appFilters[key] = value;
      }
    });
    return appFilters;
  };

  this.addFilterByName = function (name, ttl) {
    return $http.post('api/notifications/filters', null, {
      params: {
        name: name,
        ttl: ttl
      }
    });
  };

  this.addFilterById = function (id, ttl) {
    return $http.post('api/notifications/filters', null, {
      params: {
        id: id,
        ttl: ttl
      }
    });
  };

  this.removeFilter = function (id) {
    return $http.delete('api/notifications/filters/' + id);
  };
};
