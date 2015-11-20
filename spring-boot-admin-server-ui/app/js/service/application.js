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

module.exports = function ($resource, $http, $q, ApplicationLogging) {

    var isEndpointPresent = function(endpoint, configprops) {
        if (configprops[endpoint]) {
           return true;
        } else {
           return false;
        }
    };


    var Application = $resource(
        'api/applications/:id', { id: '@id' }, {
            query: { method: 'GET', isArray: true },
            get: { method: 'GET' },
            remove: { method: 'DELETE' }
    });

    var convert = function (request, isArray) {
        isArray = isArray || false;
        var deferred = $q.defer();
        request.success(function(response) {
            var result = response;
            delete result['_links'];
            if (isArray) {
                result = response.content || response;
            }
            deferred.resolve(result);
        }).error(function(response) {
            deferred.reject(response);
        });
        return deferred.promise;
    };

    Application.prototype.getCapabilities = function() {
        var application = this;
        this.capabilities = {};
        if (this.managementUrl) {
            $http.get('api/applications/' + application.id + '/configprops').success(function(configprops) {
                application.capabilities.logfile = isEndpointPresent('logfileMvcEndpoint', configprops);
                application.capabilities.activiti = isEndpointPresent('processEngineEndpoint', configprops);
                application.capabilities.restart = isEndpointPresent('restartEndpoint', configprops);
                application.capabilities.refresh = isEndpointPresent('refreshEndpoint', configprops);
                application.capabilities.pause = isEndpointPresent('pauseEndpoint', configprops);
                application.capabilities.resume = isEndpointPresent('resumeEndpoint', configprops);
            });
        }
    };

    Application.prototype.getHealth = function () {
        return convert($http.get('api/applications/' + this.id + '/health'));
    };

    Application.prototype.getInfo = function () {
        return convert($http.get('api/applications/' + this.id + '/info'));
    };

    Application.prototype.getMetrics = function () {
        return convert($http.get('api/applications/' + this.id + '/metrics'));
    };

    Application.prototype.getEnv = function (key) {
        return convert($http.get('api/applications/' + this.id + '/env' + (key ? '/' + key : '' )));
    };

    Application.prototype.setEnv = function (map) {
        return convert($http.post('api/applications/' + this.id + '/env', '', {params: map}));
    };

    Application.prototype.resetEnv = function () {
        return convert($http.post('api/applications/' + this.id + '/env/reset'));
    };

    Application.prototype.refresh = function () {
        return convert($http.post('api/applications/' + this.id + '/refresh'));
    };

    Application.prototype.getThreadDump = function () {
        return convert($http.get('api/applications/' + this.id + '/dump'), true);
    };

    Application.prototype.getTraces = function () {
        return convert($http.get('api/applications/' + this.id + '/trace'), true);
    };

    Application.prototype.getActiviti = function () {
        return convert($http.get('api/applications/' + this.id + '/activiti'));
    };

    Application.prototype.getLogging = function() {
        return ApplicationLogging.getLoggingConfigurator(this);
    };

    return Application;
};
