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

module.exports = function() {
    return {
        restrict: 'E',
        scope: {
            metric: '=forMetric',
            globalMax: '=?globalMax'
        },
        link: function(scope) {
            scope.globalMax = scope.globalMax || scope.metric.max;
            scope.minWidth = (scope.metric.min / scope.globalMax * 100).toFixed(2);
            scope.avgWidth = (scope.metric.avg / scope.globalMax * 100).toFixed(2);
            scope.valueWidth = (scope.metric.value / scope.globalMax * 100).toFixed(2);
            scope.maxWidth = (scope.metric.max / scope.globalMax * 100).toFixed(2);
        },
        template: '<div>\
            {{metric.name}} (count: {{metric.count}})   \
                <div ng-if="metric.count > 1" class="progress with-marks">\
                    <div class="mark-current" style="left: {{valueWidth}}%"></div>\
                    <div class="bar-offset" style="width:{{minWidth}}%"></div>\
                    <div class="bar bar-success" style="width: {{avgWidth - minWidth}}%;"></div>\
                    <div class="bar bar-success" style="width: {{maxWidth - avgWidth}}%"></div>\
                    <div tooltip="value {{metric.value}}" class="value-mark" style="left: {{valueWidth}}%;">{{metric.value}}</div>\
                </div>\
                <div ng-if="metric.count > 1" class="bar-scale">\
                    <div tooltip="min {{metric.min}}" class="pitch-line" style="left: {{minWidth}}%;"><small class="muted">{{metric.min}}</small></div>\
                    <div tooltip="max {{metric.max}}" class="pitch-line" style="left: {{maxWidth}}%;"><small class="muted">{{metric.max}}</small></div>\
                    <div tooltip="average {{metric.avg}}" class="pitch-line" style="left: {{avgWidth}}%;">{{metric.avg}}</div>\
                </div>\
                <div ng-if="metric.count <= 1" class="progress" style="margin-bottom: 0px;">\
                    <div class="bar bar-success" style="width: {{valueWidth}}%; text-align:right; padding-right: 5px;">{{metric.value}}</div>\
                </div>\
            </div>'
    };
};
