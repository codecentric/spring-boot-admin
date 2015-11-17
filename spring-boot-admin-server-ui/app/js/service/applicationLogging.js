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

module.exports = function ($http, $q, jolokia) {
    var findInArray = function (a, name) {
        for (var i = 0; i < a.length; i++) {
            var match = /ch.qos.logback.classic:Name=([^,]+)/.exec(a[i]);
            if (match !== null && match[1].toUpperCase() === name.toUpperCase()) {
                return a[i];
            }
        }
        return null;
    };

    var findLogbackMbean = function (app) {
        return jolokia.search('api/applications/' + app.id + '/jolokia/', 'ch.qos.logback.classic:Name=*,Type=ch.qos.logback.classic.jmx.JMXConfigurator').then(function (response) {
            if (response.value.length === 1) {
                return response.value[0];
            }
            if (response.value.length > 1) {
                //find the one with the appname or default
                var value = findInArray(response.value, app.name);
                if (value === null) {
                    value = findInArray(response.value, 'default');
                }
                if (value !== null) {
                    return value;
                }
                return $q.reject({ error:'Ambigious Logback JMXConfigurator-MBeans found!', candidates: response.value});
            }
            return $q.reject({ error: 'Couldn\'t find Logback JMXConfigurator-MBean' });
        });
    };

    this.getLoggingConfigurator = function (app) {
        return findLogbackMbean(app).then(function (logbackMbean) {
            return {
                getLoglevel: function (loggers) {
                    var requests = [];
                    for (var j in loggers) {
                        requests.push({
                            type: 'exec',
                            mbean: logbackMbean,
                            operation: 'getLoggerEffectiveLevel',
                            arguments: [loggers[j].name]
                        });
                    }
                    return jolokia.bulkRequest('api/applications/' + app.id + '/jolokia/', requests);
                },
                setLoglevel: function (logger, level) {
                    return jolokia.exec('api/applications/' + app.id + '/jolokia/', logbackMbean, 'setLoggerLevel', [logger,
                        level
                    ]);
                },
                getAllLoggers: function () {
                    return jolokia.readAttr('api/applications/' + app.id + '/jolokia/', logbackMbean, 'LoggerList');
                }
            };
        });
    };
};
