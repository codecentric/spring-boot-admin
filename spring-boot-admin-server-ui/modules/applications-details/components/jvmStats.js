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

module.exports = {
    bindings : {
        metrics : '<metrics',
    },
    controller: function($filter) {
        var ctrl = this;
        ctrl.$onChanges = function() {
            if (ctrl.metrics['systemload.average']) {
                ctrl.systemload = $filter('number')(ctrl.metrics['systemload.average'], 2);
            }
        };
    },
    template : 
          '<table class="table">'
        + '     <tr>'
        + '             <td>Uptime</td>'
        + '             <td colspan="2"><sba-uptime value="$ctrl.metrics.uptime"></sba-uptime></td>'
        + '     </tr><tr ng-if="$ctrl.systemload">'
        + '             <td>Systemload</td>'
        + '             <td colspan="2">{{$ctrl.systemload}} (last min. &#x2300; runq-sz)</td>'
        + '     </tr><tr>'
        + '             <td>Available Processors</td>'
        + '             <td colspan="2" ng-bind="$ctrl.metrics.processors"></td>'
        + '     </tr><tr>'
        + '             <td rowspan="3">Classes</td>'
        + '             <td>current loaded</td>'
        + '             <td ng-bind="$ctrl.metrics.classes"></td>'
        + '     </tr><tr>'
        + '             <td>total loaded</td>'
        + '             <td ng-bind="$ctrl.metrics[\'classes.loaded\']"></td>'
        + '     </tr><tr>'
        + '             <td>unloaded </td>'
        + '             <td ng-bind="$ctrl.metrics[\'classes.unloaded\']"></td>'
        + '     </tr><tr>'
        + '             <td rowspan="3">Threads</td>'
        + '             <td>total</td>'
        + '             <td ng-bind="$ctrl.metrics.threads"></td>'
        + '     </tr><tr>'
        + '             <td>daemon</td>'
        + '             <td ng-bind="$ctrl.metrics[\'threads.daemon\']"></td>'
        + '     </tr><tr>'
        + '             <td>peak</td>'
        + '             <td ng-bind="$ctrl.metrics[\'threads.peak\']"></td>'
        + '     </tr>'
        + '</table>'
};
