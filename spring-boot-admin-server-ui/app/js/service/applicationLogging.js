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

module.exports = function ($http, jolokia) {
    var LOGBACK_MBEAN =
        'ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator';

    this.getLoglevel = function (app, loggers) {
        var requests = [];
        for (var j in loggers) {
            requests.push({
                type: 'exec',
                mbean: LOGBACK_MBEAN,
                operation: 'getLoggerEffectiveLevel',
                arguments: [loggers[j].name]
            });
        }
        return jolokia.bulkRequest(app.url + '/jolokia/', requests);
    };

    this.setLoglevel = function (app, logger, level) {
        return jolokia.exec(app.url + '/jolokia/', LOGBACK_MBEAN, 'setLoggerLevel', [logger,
            level
        ]);
    };

    this.getAllLoggers = function (app) {
        return jolokia.readAttr(app.url + '/jolokia/', LOGBACK_MBEAN, 'LoggerList');
    };
};
