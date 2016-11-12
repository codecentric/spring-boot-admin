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

module.exports = function ($resource, $http) {
  'ngInject';

  var Application = $resource('api/applications/:id', { id: '@id' }, {
    query: {
      method: 'GET',
      isArray: true
    }, get: {
      method: 'GET'
    }, remove: {
      method: 'DELETE'
    }
  });

  var convert = function (response) {
    delete response.data['_links'];
    return response;
  };

  var convertArray = function (response) {
    delete response.data['_links'];
    response.data = response.data.content || response.data;
    return response;
  };

  Application.prototype.getHealth = function () {
    return $http.get('api/applications/' + this.id + '/health').then(convert);
  };

  Application.prototype.getInfo = function () {
    return $http.get('api/applications/' + this.id + '/info').then(convert);
  };

  Application.prototype.getMetrics = function () {
    return $http.get('api/applications/' + this.id + '/metrics').then(convert);
  };

  Application.prototype.getEnv = function (key) {
    return $http.get('api/applications/' + this.id + '/env' + (key ? '/' + key : '')).then(convert);
  };

  Application.prototype.setEnv = function (map) {
    return $http.post('api/applications/' + this.id + '/env', '', { params: map }).then(convert);
  };

  Application.prototype.resetEnv = function () {
    return $http.post('api/applications/' + this.id + '/env/reset').then(convert);
  };

  Application.prototype.refresh = function () {
    return $http.post('api/applications/' + this.id + '/refresh').then(convert);
  };

  Application.prototype.getThreadDump = function () {
    return $http.get('api/applications/' + this.id + '/dump').then(convertArray);
  };

  Application.prototype.getTraces = function () {
    return $http.get('api/applications/' + this.id + '/trace').then(convertArray);
  };

  Application.prototype.getLoggers = function () {
    return $http.get('api/applications/' + this.id + '/loggers').then(convertArray);
  };

  Application.prototype.setLoggerLevel = function (logger, level) {
    return $http.post('api/applications/' + this.id + '/loggers/' + logger, { configuredLevel: level });
  };

  return Application;
};