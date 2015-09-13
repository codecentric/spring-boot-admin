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

module.exports = function ($filter) {
    var flatten = function (obj, prefix) {
        if (obj instanceof Date) {
            obj = $filter('date')(obj, 'dd.MM.yyyy HH:mm:ss');
        }
        if (typeof obj === 'boolean' || typeof obj === 'string' || typeof obj === 'number') {
            return (prefix ? prefix + ': ' : '') + obj;
        }

        var result = '';
        var first = true;
        angular.forEach(obj, function(value, key) {
            if (angular.isString(value) && (key === 'time' || key === 'timestamp')) {
               if (/^\d+$/.test(value)) {
                  value = new Date(value * 1000);
               } else if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\+\d{4}$/.test(value)) {
                  value = new Date(value);
               }
            }

            if (first) {
                first = false;
            } else {
                result = result + '\n';
            }

            result = result + flatten(value, prefix ? prefix + '.' + key : key);
        });

        return result;
    };

    return flatten;
};
