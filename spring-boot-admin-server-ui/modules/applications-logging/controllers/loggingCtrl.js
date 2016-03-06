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

module.exports = function($scope, application, ApplicationLogging) {
    $scope.loggers = [];
    $scope.filteredLoggers = [];
    $scope.limit = 10;
    $scope.showPackageLoggers = false;

    $scope.packageFilter = function(logger) {
        return !logger.packageLogger || $scope.showPackageLoggers;
    };

    ApplicationLogging.getLoggingConfigurator(application).then(
            function(logging) {
                $scope.refreshLoggers = function(changedLogger) {
                    var outdatedLoggers = $scope.loggers.filter(function(logger) {
                        return !changedLogger || changedLogger.name === 'ROOT'
                                || logger.name.indexOf(changedLogger.name) === 0;
                    });
                    logging.getLogLevels(outdatedLoggers).catch(function(responses) {
                        responses.some(function(response) {
                            if (response.error !== null) {
                                $scope.error = response;
                                return true;
                            }
                            return false;
                        });
                    });
                };

                logging.getAllLoggers().then(function(loggers) {
                    $scope.loggers = loggers;
                }).catch(function(response) {
                    $scope.error = response;
                    $scope.errorWhileListing = true;
                });
            }).catch(function(response) {
                $scope.error = response;
                $scope.errorWhileListing = true;
            });
};
