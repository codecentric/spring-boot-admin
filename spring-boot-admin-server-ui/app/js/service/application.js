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

module.exports = function ($resource, $http, $rootScope) {
    var Application = $resource(
        'api/applications/:id', { id: '@id' }, {
            query: { method: 'GET', isArray: true },
            get: { method: 'GET' },
            remove: { method: 'DELETE' }
    });

    var AuthInterceptor = function (application) {
        return function (data, status, headers) {
            if (status === 401) {
                $rootScope.$emit('application-auth-required', application, headers('WWW-Authenticate').split(' ')[0]);
            }
        };
    };

    Application.prototype.getHealth = function () {
        return $http.get(this.url + '/health').error(new AuthInterceptor(this));
    };

    Application.prototype.getInfo = function () {
        return $http.get(this.url + '/info').error(new AuthInterceptor(this));
    };

    Application.prototype.getMetrics = function () {
        return $http.get(this.url + '/metrics').error(new AuthInterceptor(this));
    };

    Application.prototype.getEnv = function () {
        return $http.get(this.url + '/env').error(new AuthInterceptor(this));
    };

    Application.prototype.getThreadDump = function () {
        return $http.get(this.url + '/dump').error(new AuthInterceptor(this));
    };

    Application.prototype.getTraces = function () {
        return $http.get(this.url + '/trace').error(new AuthInterceptor(this));
    };

    Application.prototype.hasLogfile = function () {
        return $http.head(this.url + '/logfile').error(new AuthInterceptor(this));
    };

    return Application;
};
