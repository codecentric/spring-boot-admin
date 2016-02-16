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

module.exports = function ($scope, application) {
    $scope.loggers = [];
    $scope.filteredLoggers = [];
    $scope.limit = 10;

    application.getLogging().then(function (logging) {
        var findLogger = function (loggers, name) {
            for (var i in loggers) {
                if (loggers[i].name === name) {
                    return loggers[i];
                }
            }
            return null;
        };

        $scope.setLogLevel = function (name, level) {
            logging.setLoglevel(name, level)
                .then(function () {
                    $scope.reload(name);
                }, function (response) {
                    $scope.error = response;
                    $scope.reload(name);
                });
        };

        $scope.reload = function (prefix) {
            for (var i in $scope.loggers) {
                if (prefix === null || prefix === 'ROOT' || $scope.loggers[i].name.indexOf(
                        prefix) === 0) {
                    $scope.loggers[i].level = null;
                }
            }
            $scope.refreshLevels();
        };

        $scope.refreshLevels = function () {
            var toLoad = [];
            var slice = $scope.filteredLoggers.slice(0, $scope.limit);
            for (var i in slice) {
                if (slice[i].level === null) {
                    toLoad.push(slice[i]);
                }
            }

            if (toLoad.length === 0) {
                return;
            }

            logging.getLoglevel(toLoad)
                .then(
                    function (responses) {
                        for (var j in responses) {
                            var name = responses[j].request.arguments[0];
                            var level = responses[j].value;
                            findLogger($scope.loggers, name)
                                .level = level;
                        }
                    }, function (responses) {
                    for (var j in responses) {
                        if (responses[j].error !== null) {
                            $scope.error = responses[j];
                            break;
                        }
                    }
                });
        };

        logging.getAllLoggers()
            .then(function (response) {
                $scope.loggers = [];
                for (var i in response.value) {
                    $scope.loggers.push({
                        name: response.value[i],
                        level: null
                    });
                }

                $scope.$watchCollection('filteredLoggers', function () {
                    $scope.refreshLevels();
                });

                $scope.$watch('limit', function () {
                    $scope.refreshLevels();
                });
            }, function (response) {
                $scope.error = response;
                $scope.errorWhileListing = true;
            });
    }, function (response) {
        $scope.error = response;
        $scope.errorWhileListing = true;
    });
};
