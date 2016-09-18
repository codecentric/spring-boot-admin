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

module.exports = function () {
  return {
    restrict: 'E',
    scope: {
      metric: '=forMetric',
      globalMax: '=?globalMax'
    },
    link: function (scope) {
      scope.globalMax = scope.globalMax || scope.metric.max;
      scope.minWidth = (scope.metric.min / scope.globalMax * 100).toFixed(2);
      scope.avgWidth = (scope.metric.avg / scope.globalMax * 100).toFixed(2);
      scope.valueWidth = (scope.metric.value / scope.globalMax * 100).toFixed(2);
      scope.maxWidth = (scope.metric.max / scope.globalMax * 100).toFixed(2);
    },
    template: require('./richMetricBar.tpl.html')
  };
};
