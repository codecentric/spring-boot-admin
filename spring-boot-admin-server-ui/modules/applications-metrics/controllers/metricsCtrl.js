/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
  $scope.showRichGauges = false;
  $scope.metrics = [];

  function merge(array, obj) {
    for (var i = 0; i < array.length; i++) {
      if (array[i].name === obj.name) {
        for (var a in obj) {
          if (obj.hasOwnProperty(a)) {
            array[i][a] = obj[a];
          }
        }
        return;
      }
    }
    array.push(obj);
  }

  application.getMetrics().then(
    function (response) {
      var metricData = {};

      function addData(groupname, valueObj, max) {
        var group = metricData[groupname] || {max: 0, values: []};
        merge(group.values, valueObj);
        if (typeof max !== 'undefined' && max > group.max) {
          group.max = max;
        }
        metricData[groupname] = group;
      }

      var matchers = [{
        regex: /(gauge\..+)\.val/,
        callback: function (metric, match, value) {
          addData('gauge', {
            name: match[1],
            value: value
          }, value);
          $scope.showRichGauges = true;
        }
      }, {
        regex: /(gauge\..+)\.avg/,
        callback: function (metric, match, value) {
          addData('gauge', {
            name: match[1],
            avg: value.toFixed(2)
          });
        }
      }, {
        regex: /(gauge\..+)\.min/,
        callback: function (metric, match, value) {
          addData('gauge', {
            name: match[1],
            min: value
          });
        }
      }, {
        regex: /(gauge\..+)\.max/,
        callback: function (metric, match, value) {
          addData('gauge', {
            name: match[1],
            max: value
          }, value);
        }
      }, {
        regex: /(gauge\..+)\.count/,
        callback: function (metric, match, value) {
          addData('gauge', {
            name: match[1],
            count: value
          });
        }
      }, {
        regex: /(gauge\..+)\.alpha/,
        callback: function () { /* NOP */
        }
      }, {
        regex: /(gauge\..+)/,
        callback: function (metric, match, value) {
          addData('gauge', {
            name: match[1],
            value: value
          }, value);
        }
      }, {
        regex: /^([^.]+).*/,
        callback: function (metric, match, value) {
          addData(match[1], {
            name: metric,
            value: value
          }, value);
        }
      }];

      var metrics = response.data;
      for (var metric in metrics) {
        if (metrics.hasOwnProperty(metric)) {
          for (var i = 0; i < matchers.length; i++) {
            var match = matchers[i].regex.exec(metric);
            if (match !== null) {
              matchers[i].callback(metric, match, metrics[metric]);
              break;
            }
          }
        }
      }
      for (var groupname in metricData) {
        if (metricData.hasOwnProperty(groupname)) {
          $scope.metrics.push({
            name: groupname,
            values: metricData[groupname].values,
            max: metricData[groupname].max || 0
          });
        }
      }
    }
  ).catch(function (response) {
    $scope.error = response.data;
  });
};
