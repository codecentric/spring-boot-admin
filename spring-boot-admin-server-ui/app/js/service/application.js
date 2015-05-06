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

module.exports = function ($resource, $http) {

    var isEndpointEnabled = function(endpoint, configprops) {
        if (configprops[endpoint]) {
           return configprops[endpoint].properties.enabled;
        }
        return false;
    };

    var getCapabilities = function(application) {
        application.capabilities = {};
        $http.get('api/applications/' + application.id + '/configprops').success(function(configprops) {
            application.capabilities.logfile = isEndpointEnabled('logfileEndpoint', configprops);
            application.capabilities.activiti = isEndpointEnabled('processEngineEndpoint', configprops);
        });
    };

    var Application = $resource(
        'api/applications/:id', { id: '@id' }, {
            query: { method: 'GET',
                     isArray: true,
                     transformResponse: function(data) {
                         var apps = angular.fromJson(data);
                         for (var i = 0; i < apps.length; i++) {
                           getCapabilities(apps[i]);
                         }
                         return apps;
                     }
                   },
            get: { method: 'GET',
                   transformResponse: function(data) {
                       var app = angular.fromJson(data);
                       getCapabilities(app);
                       return app;
                   }
                 },
            remove: { method: 'DELETE' }
    });

    Application.prototype.getHealth = function () {
        return $http.get('api/applications/' + this.id + '/health');
    };

    Application.prototype.getInfo = function () {
        return $http.get('api/applications/' + this.id + '/info');
    };

    Application.prototype.getMetrics = function () {
        return $http.get('api/applications/' + this.id + '/metrics');
    };

    Application.prototype.getEnv = function () {
        return $http.get('api/applications/' + this.id + '/env');
    };

    Application.prototype.getThreadDump = function () {
        return $http.get('api/applications/' + this.id + '/dump');
    };

    Application.prototype.getTraces = function () {
        return $http.get('api/applications/' + this.id + '/trace');
    };

    Application.prototype.getActiviti = function () {
        return $http.get('api/applications/' + this.id + '/activiti');
    };

    return Application;
};
