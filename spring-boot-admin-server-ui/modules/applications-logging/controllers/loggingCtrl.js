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

module.exports = function ($scope, application, ApplicationLogging) {
  'ngInject';

  $scope.loggers = [];
  $scope.filteredLoggers = [];
  $scope.limit = 10;
  $scope.error = null;
  $scope.showPackageLoggers = false;

  $scope.packageFilter = function (logger) {
    return !logger.packageLogger || $scope.showPackageLoggers;
  };

  $scope.togglePackageLoggers = function () {
    $scope.showPackageLoggers = !$scope.showPackageLoggers;
  };

  var Logger = function (name, data, levels) {
    this.name = name;
    this.level = data.configuredLevel || data.effectiveLevel;
    this.levels = levels;
    var i = name.lastIndexOf('.') + 1;
    this.packageLogger = name.charAt(i) !== name.charAt(i).toUpperCase();

    this.setLevel = function (newLevel) {
      return application.setLoggerLevel(name, newLevel);
    };
  };

  $scope.refreshLoggers = function () {
    return application.getLoggers().then(function (response) {
      $scope.error = null;
      var loggers = [];
      var levels = response.data.levels.reverse();
      angular.forEach(response.data.loggers, function (value, key) {
        loggers.push(new Logger(key, value, levels));
      });
      $scope.loggers = loggers;
    }).catch(function (response) {
      $scope.error = response;
    });
  };

  $scope.refreshLoggers().then(function () {
    if ($scope.error != null && $scope.error.status === 404) {
      //in case the client is a boot 1.4 application use the old jolokia style.
      $scope.error = null;
      initializeLoggersViaJolokia();
    }
  });

  var initializeLoggersViaJolokia = function () {
    ApplicationLogging.getLoggingConfigurator(application).then(
      function (logging) {
        $scope.refreshLoggers = function (changedLogger) {
          var outdatedLoggers = $scope.loggers.filter(function (logger) {
            return !changedLogger || changedLogger.name === 'ROOT' || logger.name.indexOf(changedLogger.name) === 0;
          });
          logging.getLogLevels(outdatedLoggers).catch(function (responses) {
            responses.some(function (response) {
              if (response.error !== null) {
                $scope.error = response;
                return true;
              }
              return false;
            });
          });
        };

        logging.getAllLoggers().then(function (loggers) {
          $scope.loggers = loggers;
        }).catch(function (response) {
          $scope.error = response;
        });
      }
    ).catch(function (response) {
      $scope.error = response;
    });
  };

};
