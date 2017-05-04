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
  'ngInject';

  var metricData = {'gauge': []};
  var metricDataMax = {'gauge': 0};
  $scope.showRichGauges = false;
  $scope.metrics = [];

  application.getMetrics().then(
    function (response) {
      function merge(array, obj) {
        for (var i = 0; i < array.length; i++) {
          if (array[i].name === obj.name) {
            for (var a in obj) {
              array[i][a] = obj[a];
            }
            return;
          }
        }
        array.push(obj);
      }

      var find = function (metrics, regexes, callbacks) {
        for (var metric in metrics) {
          if(metrics.hasOwnProperty(metric)) {
            for (var i = 0; i < regexes.length; i++) {
              var match = regexes[i].exec(metric);
              if (match !== null) {
                callbacks[i](metric, match, metrics[metric]);
                break;
              }
            }
          }
        }

        for (var key in metricData) {
          if(metricData.hasOwnProperty(key)) {
            $scope.metrics.push({
              name: key,
              values: metricData[key],
              max: metricDataMax[key] || 0
            });
          }
        }
      };

      var regexes = [/^(?!gauge)([a-zA-Z0-9]+)\..*/gi, /(gauge\..+)\.val/, /(gauge\..+)\.avg/, /(gauge\..+)\.min/, /(gauge\..+)\.max/, /(gauge\..+)\.count/, /(gauge\..+)\.alpha/, /(gauge\..+)/];

      var callbacks = [
        function (metric, match, value) {
          if(typeof metricData[match[1]] === 'undefined'){
            metricData[match[1]] = [];
          }
          if(typeof metricDataMax[match[1]] === 'undefined'){
            metricDataMax[match[1]] = 0;
          }
          if (match.length >= 2 && match[1] !== null) {
            metricData[match[1]].push({
              name: metric,
              value: value
            });

            if (value > metricDataMax[match[1]]) {
              metricDataMax[match[1]] = value;
            }
          }
        },
        function (metric, match, value) {
          merge(metricData['gauge'], {
            name: match[1],
            value: value
          });
          $scope.showRichGauges = true;
          if (value > metricDataMax['gauge']) {
            metricDataMax['gauge'] = value;
          }
        },
        function (metric, match, value) {
          merge(metricData['gauge'], {
            name: match[1],
            avg: value.toFixed(2)
          });
        },
        function (metric, match, value) {
          merge(metricData['gauge'], {
            name: match[1],
            min: value
          });
        },
        function (metric, match, value) {
          merge(metricData['gauge'], {
            name: match[1],
            max: value
          });
          if (value > metricDataMax['gauge']) {
            metricDataMax['gauge'] = value;
          }
        },
        function (metric, match, value) {
          merge(metricData['gauge'], {
            name: match[1],
            count: value
          });
        },
        function () { /* NOP */
        },
        function (metric, match, value) {
          merge(metricData['gauge'], {
            name: match[1],
            value: value
          });
          if (value > metricDataMax['gauge']) {
            metricDataMax['gauge'] = value;
          }
        }];

      find(response.data, regexes, callbacks);
    }
  ).catch(function (response) {
    $scope.error = response.data;
  });
};
