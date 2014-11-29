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

module.exports = function ($http) {
    this.getInfo = function (app) {
        return $http.get(app.url + '/info/')
            .success(function (response) {
                app.version = response.version;
                delete response.version;
                app.info = response;
            })
            .error(function () {
                app.version = '---';
            });
    };
    this.getHealth = function (app) {
        return $http.get(app.url + '/health/')
            .success(function (response) {
                app.status = response.status;
            })
            .error(function (response, httpStatus) {
                if (httpStatus === 503) {
                    app.status = response.status;
                } else if (httpStatus === 404 || httpStatus === 0) {
                    app.status = 'OFFLINE';
                } else {
                    app.status = 'UNKNOWN';
                }
            });
    };
    this.getLogfile = function (app) {
        return $http.head(app.url + '/logfile/')
            .success(function () {
                app.providesLogfile = true;
            })
            .error(function () {
                app.providesLogfile = false;
            });
    };
};
