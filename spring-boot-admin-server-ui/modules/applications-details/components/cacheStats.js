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
module.exports = {
    bindings : {
        metrics : '<metrics',
    },
    controller: function($filter) {
        var ctrl = this;
        ctrl.$onChanges = function() {
            ctrl.caches = [];
            angular.forEach(ctrl.metrics, function (value, key){
                var match = /cache\.(.+)\.size/.exec(key);
                if (match !== null) {
                    ctrl.caches.push( {
                        name : match[1],
                        size : value,
                        hitRatio : $filter('number')(ctrl.metrics['cache.' + match[1] + '.hit.ratio'] * 100, 2),
                        missRatio : $filter('number')(ctrl.metrics['cache.' + match[1] + '.miss.ratio'] * 100, 2)
                    });
                }
            });
        };
        ctrl.getBarClass = function(percentage) {
            if (percentage < 75) {
                return 'bar-success';
            } else if (percentage >= 75 && percentage <= 95) {
                return 'bar-warning';
            } else {
                return 'bar-danger';
            }
        };
    },
    template : 
        '<table class="table">'
        + '     <tr ng-repeat-start="cache in $ctrl.caches track by cache.name">'
        + '             <td rowspan="2" ng-bind="cache.name"></td>'
        + '             <td>size</td>'
        + '             <td ng-bind="cache.size">'
        + '     </tr><tr ng-repeat-end ng-if="cache.hitRatio || cache.missRatio">'
        + '             <td colspan="2">'
        + '                     <div class="progress" style="margin-bottom: 0px;">'
        + '                             <div ng-if="cache.hitRatio" class="bar bar-success" style="width:{{cache.hitRatio}}%;">'
        + '                                     {{cache.hitRatio}}% hits'
        + '                             </div>'
        + '                             <div ng-if="cache.missRatio" class="bar bar-danger" style="width:{{cache.missRatio}}%;">'
        + '                                     {{cache.missRatio}}% misses'
        + '                             </div>'
        + '                     </div>'
        + '             </td>'
        + '     </tr>'
        + '</table>'
};
