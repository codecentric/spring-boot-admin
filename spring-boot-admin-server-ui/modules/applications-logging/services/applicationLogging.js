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

module.exports = function ($q, jolokia) {
  'ngInject';

  var findInArray = function (a, name) {
    return a.find(function (element) {
      var match = /ch.qos.logback.classic:Name=([^,]+)/.exec(element);
      return match !== null && match[1].toUpperCase() === name.toUpperCase();
    });
  };

  var findLogbackMbean = function (app) {
    return jolokia.search('api/applications/' + app.id + '/jolokia/',
      'ch.qos.logback.classic:Name=*,Type=ch.qos.logback.classic.jmx.JMXConfigurator')
      .then(function (response) {
        if (response.value.length === 1) {
          return response.value[0];
        }
        if (response.value.length > 1) {
          // find the one with the appname or default
          var value = findInArray(response.value, app.name);
          if (value === undefined) {
            value = findInArray(response.value, 'default');
          }
          if (value === undefined) {
            return $q.reject({
              error: 'Ambigious Logback JMXConfigurator-MBeans found!',
              candidates: response.value
            });
          }
          return value;
        }
        return $q.reject({
          error: 'Couldn\'t find Logback JMXConfigurator-MBean'
        });
      });
  };

  this.getLoggingConfigurator = function (app) {
    var LoggingConfigurator = function (logbackMbean) {
      var self = this;
      var Logger = function (name) {
        this.name = name;
        this.level = null;
        this.levels = ['TRACE', 'DEBUG', 'WARN', 'INFO', 'ERROR', 'OFF']; //Logback Default
        var i = name.lastIndexOf('.') + 1;
        this.packageLogger = name.charAt(i) !== name.charAt(i).toUpperCase();

        this.setLevel = function (newLevel) {
          return jolokia.exec('api/applications/' + app.id + '/jolokia/', logbackMbean,
            'setLoggerLevel', [name, newLevel]);
        };
      };

      this.getLogLevels = function (loggers) {
        if (loggers.length === 0) {
          var deferred = $q.defer();
          deferred.resolve(true);
          return deferred.promise;
        }
        var requests = loggers.map(function (logger) {
          return {
            type: 'exec',
            mbean: logbackMbean,
            operation: 'getLoggerEffectiveLevel',
            arguments: [logger.name]
          };
        });
        return jolokia.bulkRequest('api/applications/' + app.id + '/jolokia/', requests)
          .then(function (responses) {
            responses.forEach(function (response) {
              var logger = loggers.find(function (l) {
                return l.name === response.request.arguments[0];
              });
              if (logger) {
                logger.level = response.value;
              }
            });
            return true;
          });
      };

      this.getAllLoggers = function () {
        return jolokia.readAttr('api/applications/' + app.id + '/jolokia/', logbackMbean, 'LoggerList').then(function (response) {
          return response.value.map(function (name) {
            return new Logger(name);
          });
        }).then(function (loggers) {
          return self.getLogLevels(loggers).then(function () {
            return loggers;
          }).catch(function (error) {
            $q.reject(error);
          });
        });
      };
    };

    return findLogbackMbean(app).then(function (logbackMbean) {
      return new LoggingConfigurator(logbackMbean);
    });
  };
};
